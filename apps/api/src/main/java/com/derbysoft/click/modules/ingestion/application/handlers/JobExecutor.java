package com.derbysoft.click.modules.ingestion.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.googleadsmanagement.api.ports.GoogleAdsQueryPort;
import com.derbysoft.click.modules.ingestion.application.ports.GoogleAdsReportingPort;
import com.derbysoft.click.modules.ingestion.application.ports.GoogleAdsReportingPort.CampaignRow;
import com.derbysoft.click.modules.ingestion.domain.RawSnapshotRepository;
import com.derbysoft.click.modules.ingestion.domain.SyncJobRepository;
import com.derbysoft.click.modules.ingestion.domain.aggregates.RawSnapshot;
import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncJob;
import com.derbysoft.click.modules.googleadsmanagement.api.events.AccessFailureObserved;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.SyncJobStatus;
import com.derbysoft.click.modules.ingestion.infrastructure.googleads.IngestionAuthException;
import com.derbysoft.click.modules.ingestion.infrastructure.googleads.IngestionFetchException;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.entity.RawCampaignRowEntity;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.RawCampaignRowJpaRepository;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobExecutor {

    private static final Logger log = LoggerFactory.getLogger(JobExecutor.class);

    private final SyncJobRepository syncJobRepository;
    private final RawSnapshotRepository rawSnapshotRepository;
    private final RawCampaignRowJpaRepository rawCampaignRowJpaRepository;
    private final GoogleAdsReportingPort googleAdsReportingPort;
    private final GoogleAdsQueryPort googleAdsQueryPort;
    private final InProcessEventBus eventBus;
    private final RetryPolicyEngine retryPolicyEngine;
    private final IncidentLifecycleService incidentLifecycleService;

    public JobExecutor(
        SyncJobRepository syncJobRepository,
        RawSnapshotRepository rawSnapshotRepository,
        RawCampaignRowJpaRepository rawCampaignRowJpaRepository,
        GoogleAdsReportingPort googleAdsReportingPort,
        GoogleAdsQueryPort googleAdsQueryPort,
        InProcessEventBus eventBus,
        RetryPolicyEngine retryPolicyEngine,
        IncidentLifecycleService incidentLifecycleService
    ) {
        this.syncJobRepository = syncJobRepository;
        this.rawSnapshotRepository = rawSnapshotRepository;
        this.rawCampaignRowJpaRepository = rawCampaignRowJpaRepository;
        this.googleAdsReportingPort = googleAdsReportingPort;
        this.googleAdsQueryPort = googleAdsQueryPort;
        this.eventBus = eventBus;
        this.retryPolicyEngine = retryPolicyEngine;
        this.incidentLifecycleService = incidentLifecycleService;
    }

    @Transactional
    public void execute(UUID jobId) {
        SyncJob job = syncJobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalStateException("SyncJob not found: " + jobId));

        if (job.getStatus() != SyncJobStatus.PENDING) {
            log.debug("Skipping job {} — status is {}", jobId, job.getStatus());
            return;
        }

        Instant now = Instant.now();
        job.acquireLease(now);
        syncJobRepository.save(job);

        try {
            var connectionInfo = googleAdsQueryPort.findConnectionByTenantId(job.getTenantId())
                .orElseThrow(() -> new IllegalStateException(
                    "No Google Ads connection for tenant: " + job.getTenantId()));

            String managerId = connectionInfo.managerId();

            List<CampaignRow> rows = googleAdsReportingPort.fetchCampaignMetrics(
                job.getAccountId(), managerId, null, job.getDateWindow()
            );

            Instant ingestedAt = Instant.now();
            List<RawCampaignRowEntity> rowEntities = rows.stream()
                .map(r -> new RawCampaignRowEntity(
                    UUID.randomUUID(), null, job.getIntegrationId(), job.getAccountId(),
                    r.campaignId(), r.campaignName(), r.reportDate(),
                    r.clicks(), r.impressions(), r.costMicros(), r.conversions(), ingestedAt
                ))
                .toList();
            rawCampaignRowJpaRepository.upsertAll(rowEntities);

            String checksum = computeChecksum(rows);

            RawSnapshot snapshot = RawSnapshot.create(
                UUID.randomUUID(), job.getId(), job.getIntegrationId(), job.getTenantId(),
                job.getAccountId(), job.getReportType(), job.getDateWindow(),
                rows.size(), checksum, Instant.now()
            );
            RawSnapshot savedSnapshot = rawSnapshotRepository.save(snapshot);
            publishAndClear(savedSnapshot);

            rawCampaignRowJpaRepository.updateSnapshotIdForJob(
                savedSnapshot.getId(), job.getIntegrationId(), job.getAccountId(),
                job.getDateWindow().from(), job.getDateWindow().to()
            );

            now = Instant.now();
            job.markSucceeded(now);
            syncJobRepository.save(job);
            publishAndClear(job);

            incidentLifecycleService.onSuccess(job.getIdempotencyKey(), job.getTenantId());

        } catch (IngestionAuthException | IngestionFetchException e) {
            FailureClass fc = retryPolicyEngine.classify(e);
            now = Instant.now();
            job.markFailed(fc, e.getMessage(), now);
            syncJobRepository.save(job);
            publishAndClear(job);

            if (e instanceof IngestionAuthException iae) {
                eventBus.publish(EventEnvelope.of("AccessFailureObserved",
                    new AccessFailureObserved(job.getTenantId(), job.getAccountId(),
                        iae.getMessage())));
            }

            incidentLifecycleService.onFailure(job.getIdempotencyKey(), job.getTenantId(), fc);

            if (job.canRetry()) {
                var delay = retryPolicyEngine.computeDelay(job);
                job.requeueForRetry(now.plus(delay), now);
                syncJobRepository.save(job);
            }
        }
    }

    private String computeChecksum(List<CampaignRow> rows) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            rows.stream()
                .sorted(Comparator.comparing(CampaignRow::campaignId)
                    .thenComparing(r -> r.reportDate().toString()))
                .forEach(r -> {
                    String entry = r.campaignId() + r.reportDate() + r.clicks()
                        + r.impressions() + r.costMicros();
                    digest.update(entry.getBytes(StandardCharsets.UTF_8));
                });
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            log.warn("Failed to compute checksum: {}", e.getMessage());
            return "unknown";
        }
    }

    private void publishAndClear(SyncJob job) {
        job.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        job.clearEvents();
    }

    private void publishAndClear(RawSnapshot snapshot) {
        snapshot.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        snapshot.clearEvents();
    }
}
