package com.derbysoft.click.modules.normalisation.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.normalisation.application.ports.RawCampaignRowQueryPort;
import com.derbysoft.click.modules.normalisation.application.ports.RawCampaignRowQueryPort.RawCampaignRowData;
import com.derbysoft.click.modules.normalisation.application.services.BatchAssembler;
import com.derbysoft.click.modules.normalisation.application.services.IdempotencyGuard;
import com.derbysoft.click.modules.normalisation.application.services.Normalizer;
import com.derbysoft.click.modules.normalisation.application.services.QualityValidator;
import com.derbysoft.click.modules.normalisation.domain.CanonicalBatchRepository;
import com.derbysoft.click.modules.normalisation.domain.CanonicalFactRepository;
import com.derbysoft.click.modules.normalisation.domain.aggregates.CanonicalBatch;
import com.derbysoft.click.modules.normalisation.domain.events.CanonicalFactQuarantined;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.MappingVersion;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.QualityFlag;
import com.derbysoft.click.modules.normalisation.infrastructure.persistence.entity.CanonicalFactEntity;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NormalisationService {

    private static final Logger log = LoggerFactory.getLogger(NormalisationService.class);

    private final CanonicalBatchRepository batchRepository;
    private final CanonicalFactRepository factRepository;
    private final RawCampaignRowQueryPort rawCampaignRowQueryPort;
    private final Normalizer normalizer;
    private final QualityValidator qualityValidator;
    private final BatchAssembler batchAssembler;
    private final IdempotencyGuard idempotencyGuard;
    private final InProcessEventBus eventBus;

    public NormalisationService(
        CanonicalBatchRepository batchRepository,
        CanonicalFactRepository factRepository,
        RawCampaignRowQueryPort rawCampaignRowQueryPort,
        Normalizer normalizer,
        QualityValidator qualityValidator,
        BatchAssembler batchAssembler,
        IdempotencyGuard idempotencyGuard,
        InProcessEventBus eventBus
    ) {
        this.batchRepository = batchRepository;
        this.factRepository = factRepository;
        this.rawCampaignRowQueryPort = rawCampaignRowQueryPort;
        this.normalizer = normalizer;
        this.qualityValidator = qualityValidator;
        this.batchAssembler = batchAssembler;
        this.idempotencyGuard = idempotencyGuard;
        this.eventBus = eventBus;
    }

    @Transactional
    public CanonicalBatch normalizeSnapshot(UUID snapshotId, UUID integrationId, UUID tenantId,
                                             String accountId, MappingVersion mappingVersion) {
        // 1. Idempotency check
        var existing = idempotencyGuard.check(snapshotId, mappingVersion);
        if (existing.isPresent()) {
            log.debug("Snapshot {} already normalised (batch {}), skipping", snapshotId, existing.get().getId());
            return existing.get();
        }

        Instant now = Instant.now();

        // 2. Create batch in PROCESSING
        CanonicalBatch batch = CanonicalBatch.create(snapshotId, integrationId, tenantId, accountId, mappingVersion, now);
        batch = batchRepository.save(batch);
        publishAndClear(batch);

        try {
            // 3. Fetch raw rows
            List<RawCampaignRowData> rows = rawCampaignRowQueryPort.findBySnapshotId(snapshotId);

            // 4. Map + validate each row
            List<CanonicalFactEntity> facts = new ArrayList<>();
            int quarantinedCount = 0;

            for (RawCampaignRowData row : rows) {
                List<QualityFlag> flags = qualityValidator.validate(row);
                CanonicalFactEntity fact = normalizer.map(row, batch.getId(), mappingVersion, now);
                fact.setTenantId(tenantId);

                if (!flags.isEmpty()) {
                    String[] flagNames = flags.stream().map(Enum::name).toArray(String[]::new);
                    fact.setQualityFlags(flagNames);
                    fact.setQuarantined(true);
                    quarantinedCount++;

                    eventBus.publish(EventEnvelope.of("CanonicalFactQuarantined",
                        new CanonicalFactQuarantined(fact.getId(), batch.getId(),
                            row.campaignId(), row.reportDate(), flags, now)));
                }
                facts.add(fact);
            }

            // 5. Compute checksum
            String checksum = batchAssembler.computeChecksum(facts);

            // 6. Persist facts
            factRepository.saveAll(facts);

            // 7. Produce batch
            final int finalQuarantinedCount = quarantinedCount;
            final int finalFactCount = facts.size();
            final String finalChecksum = checksum;
            final UUID batchId = batch.getId();

            // Re-fetch to get the saved batch for state transition
            batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalStateException("Batch disappeared: " + batchId));
            batch.produce(finalFactCount, finalQuarantinedCount, finalChecksum, Instant.now());
            batch = batchRepository.save(batch);
            publishAndClear(batch);

            log.info("Normalised snapshot {} → batch {} ({} facts, {} quarantined)",
                snapshotId, batchId, finalFactCount, finalQuarantinedCount);
            return batch;

        } catch (Exception e) {
            log.error("Normalisation failed for snapshot {}: {}", snapshotId, e.getMessage(), e);
            // Re-fetch batch to apply failure
            UUID batchId = batch.getId();
            batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalStateException("Batch disappeared: " + batchId));
            batch.fail(e.getMessage(), Instant.now());
            batch = batchRepository.save(batch);
            publishAndClear(batch);
            throw new RuntimeException("Normalisation failed for snapshot " + snapshotId, e);
        }
    }

    private void publishAndClear(CanonicalBatch batch) {
        batch.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        batch.clearEvents();
    }
}
