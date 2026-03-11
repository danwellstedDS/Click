package com.derbysoft.click.modules.attributionmapping.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.attributionmapping.application.ports.AccountBindingQueryPort;
import com.derbysoft.click.modules.attributionmapping.application.ports.AccountBindingQueryPort.ActiveBindingData;
import com.derbysoft.click.modules.attributionmapping.application.ports.CanonicalFactQueryPort;
import com.derbysoft.click.modules.attributionmapping.application.ports.CanonicalFactQueryPort.CanonicalFactData;
import com.derbysoft.click.modules.attributionmapping.application.services.IdempotencyGuard;
import com.derbysoft.click.modules.attributionmapping.application.services.MappingEngine;
import com.derbysoft.click.modules.attributionmapping.application.services.OverrideSetVersionComputer;
import com.derbysoft.click.modules.attributionmapping.domain.MappedFactRepository;
import com.derbysoft.click.modules.attributionmapping.domain.MappingRunRepository;
import com.derbysoft.click.modules.attributionmapping.domain.aggregates.MappingRun;
import com.derbysoft.click.modules.attributionmapping.domain.valueobjects.ConfidenceBand;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappedFactEntity;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttributionService {

    private static final Logger log = LoggerFactory.getLogger(AttributionService.class);

    private final MappingRunRepository runRepository;
    private final MappedFactRepository factRepository;
    private final CanonicalFactQueryPort canonicalFactQueryPort;
    private final AccountBindingQueryPort accountBindingQueryPort;
    private final MappingEngine mappingEngine;
    private final IdempotencyGuard idempotencyGuard;
    private final OverrideSetVersionComputer overrideSetVersionComputer;
    private final InProcessEventBus eventBus;

    public AttributionService(
        MappingRunRepository runRepository,
        MappedFactRepository factRepository,
        CanonicalFactQueryPort canonicalFactQueryPort,
        AccountBindingQueryPort accountBindingQueryPort,
        MappingEngine mappingEngine,
        IdempotencyGuard idempotencyGuard,
        OverrideSetVersionComputer overrideSetVersionComputer,
        InProcessEventBus eventBus
    ) {
        this.runRepository = runRepository;
        this.factRepository = factRepository;
        this.canonicalFactQueryPort = canonicalFactQueryPort;
        this.accountBindingQueryPort = accountBindingQueryPort;
        this.mappingEngine = mappingEngine;
        this.idempotencyGuard = idempotencyGuard;
        this.overrideSetVersionComputer = overrideSetVersionComputer;
        this.eventBus = eventBus;
    }

    @Transactional
    public MappingRun mapBatch(UUID canonicalBatchId, UUID tenantId, String ruleSetVersion) {
        // 1. Compute override set version
        String overrideSetVersion = overrideSetVersionComputer.compute(tenantId);

        // 2. Idempotency check
        var existing = idempotencyGuard.check(canonicalBatchId, ruleSetVersion, overrideSetVersion);
        if (existing.isPresent()) {
            log.debug("Batch {} already mapped (run {}), skipping", canonicalBatchId, existing.get().getId());
            return existing.get();
        }

        Instant now = Instant.now();

        // 3. Create run in RUNNING
        MappingRun run = MappingRun.create(canonicalBatchId, tenantId, ruleSetVersion, overrideSetVersion, now);
        run = runRepository.save(run);
        publishAndClear(run);

        try {
            // 4. Fetch canonical facts
            List<CanonicalFactData> facts = canonicalFactQueryPort.findByBatchId(canonicalBatchId);

            // 5. Fetch active bindings indexed by customerId
            List<ActiveBindingData> bindings = accountBindingQueryPort.findActiveByTenantId(tenantId);
            Map<String, ActiveBindingData> bindingIndex = MappingEngine.indexByCustomerId(bindings);

            // 6. Resolve each fact
            List<MappedFactEntity> mappedFacts = new ArrayList<>();
            List<UUID> lowConfidenceFactIds = new ArrayList<>();
            int lowConfidenceCount = 0;
            int unresolvedCount = 0;

            UUID runId = run.getId();
            for (CanonicalFactData fact : facts) {
                MappedFactEntity mapped = mappingEngine.resolve(fact, bindingIndex, runId, ruleSetVersion, now);
                mappedFacts.add(mapped);

                String band = mapped.getConfidenceBand();
                if (ConfidenceBand.UNRESOLVED.name().equals(band)) {
                    unresolvedCount++;
                    lowConfidenceFactIds.add(mapped.getId());
                } else if (ConfidenceBand.LOW.name().equals(band)) {
                    lowConfidenceCount++;
                    lowConfidenceFactIds.add(mapped.getId());
                }
            }

            // 7. Persist facts
            factRepository.saveAll(mappedFacts);

            // 8. Produce run
            final int finalMappedCount = mappedFacts.size();
            final int finalLowConfidenceCount = lowConfidenceCount;
            final int finalUnresolvedCount = unresolvedCount;
            final List<UUID> finalLowConfidenceFactIds = List.copyOf(lowConfidenceFactIds);

            run = runRepository.findById(runId)
                .orElseThrow(() -> new IllegalStateException("MappingRun disappeared: " + runId));
            run.produce(finalMappedCount, finalLowConfidenceCount, finalUnresolvedCount,
                        finalLowConfidenceFactIds, Instant.now());
            run = runRepository.save(run);
            publishAndClear(run);

            log.info("Mapped batch {} → run {} ({} facts, {} low-confidence, {} unresolved)",
                canonicalBatchId, runId, finalMappedCount, finalLowConfidenceCount, finalUnresolvedCount);
            return run;

        } catch (Exception e) {
            log.error("Attribution failed for batch {}: {}", canonicalBatchId, e.getMessage(), e);
            UUID runId = run.getId();
            run = runRepository.findById(runId)
                .orElseThrow(() -> new IllegalStateException("MappingRun disappeared: " + runId));
            run.fail(e.getMessage(), Instant.now());
            run = runRepository.save(run);
            publishAndClear(run);
            throw new RuntimeException("Attribution failed for batch " + canonicalBatchId, e);
        }
    }

    private void publishAndClear(MappingRun run) {
        run.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        run.clearEvents();
    }
}
