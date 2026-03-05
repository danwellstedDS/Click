package com.derbysoft.click.modules.ingestion.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.googleadsmanagement.api.contracts.AccountBindingInfo;
import com.derbysoft.click.modules.googleadsmanagement.api.ports.GoogleAdsQueryPort;
import com.derbysoft.click.modules.ingestion.application.handlers.RateLimitService.RateLimitResult;
import com.derbysoft.click.modules.ingestion.domain.SyncIncidentRepository;
import com.derbysoft.click.modules.ingestion.domain.SyncJobRepository;
import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncIncident;
import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncJob;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.DateWindow;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.TriggerType;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IngestionJobService {

    private static final String REPORT_TYPE = "CAMPAIGN_PERFORMANCE";

    private final SyncJobRepository syncJobRepository;
    private final SyncIncidentRepository incidentRepository;
    private final RateLimitService rateLimitService;
    private final GoogleAdsQueryPort googleAdsQueryPort;
    private final InProcessEventBus eventBus;

    public IngestionJobService(
        SyncJobRepository syncJobRepository,
        SyncIncidentRepository incidentRepository,
        RateLimitService rateLimitService,
        GoogleAdsQueryPort googleAdsQueryPort,
        InProcessEventBus eventBus
    ) {
        this.syncJobRepository = syncJobRepository;
        this.incidentRepository = incidentRepository;
        this.rateLimitService = rateLimitService;
        this.googleAdsQueryPort = googleAdsQueryPort;
        this.eventBus = eventBus;
    }

    public void enqueueDailySync(UUID integrationId, UUID tenantId) {
        Instant now = Instant.now();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate yesterday = today.minusDays(1);
        LocalDate from = today.minusDays(3);
        DateWindow dateWindow = new DateWindow(from, yesterday);

        List<AccountBindingInfo> bindings = googleAdsQueryPort.listActiveBindings(tenantId);

        for (AccountBindingInfo binding : bindings) {
            String status = binding.status();
            if ("BROKEN".equals(status) || "REMOVED".equals(status)) {
                continue;
            }

            String accountId = binding.customerId();
            UUID bindingId = binding.id();
            String idempotencyKey = bindingId + ":" + accountId + ":"
                + from + ":" + yesterday + ":" + REPORT_TYPE;

            Optional<SyncJob> existing = syncJobRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                String existingStatus = existing.get().getStatus().name();
                if ("PENDING".equals(existingStatus) || "RUNNING".equals(existingStatus)) {
                    continue;
                }
            }

            SyncJob job = SyncJob.create(
                UUID.randomUUID(), bindingId, tenantId, accountId, REPORT_TYPE,
                dateWindow, TriggerType.DAILY, null, "daily-scheduler", now
            );
            SyncJob saved = syncJobRepository.save(job);
            publishAndClear(saved);
        }
    }

    public SyncJob enqueueManualSync(UUID tenantId, UUID integrationId, String accountId,
                                      String reason, String triggeredBy) {
        RateLimitResult result = rateLimitService.checkAndRecord(tenantId);
        if (!result.allowed()) {
            throw new DomainError.ValidationError("INGEST_429",
                "Rate limit exceeded. Retry after " + result.retryAfterSeconds() + " seconds.");
        }

        Instant now = Instant.now();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        DateWindow dateWindow = new DateWindow(today.minusDays(3), today.minusDays(1));

        SyncJob job = SyncJob.create(
            UUID.randomUUID(), integrationId, tenantId, accountId, REPORT_TYPE,
            dateWindow, TriggerType.MANUAL, triggeredBy, reason, now
        );
        SyncJob saved = syncJobRepository.save(job);
        publishAndClear(saved);
        return saved;
    }

    public SyncJob enqueueBackfill(UUID tenantId, UUID integrationId, String accountId,
                                    DateWindow dateRange, String reason, String triggeredBy) {
        if (dateRange.days() > 14) {
            throw new DomainError.ValidationError("INGEST_400",
                "Backfill date range must not exceed 14 days; requested: " + dateRange.days() + " days.");
        }

        RateLimitResult result = rateLimitService.checkAndRecord(tenantId);
        if (!result.allowed()) {
            throw new DomainError.ValidationError("INGEST_429",
                "Rate limit exceeded. Retry after " + result.retryAfterSeconds() + " seconds.");
        }

        Instant now = Instant.now();
        SyncJob job = SyncJob.create(
            UUID.randomUUID(), integrationId, tenantId, accountId, REPORT_TYPE,
            dateRange, TriggerType.BACKFILL, triggeredBy, reason, now
        );
        SyncJob saved = syncJobRepository.save(job);
        publishAndClear(saved);
        return saved;
    }

    public SyncJob forceRun(UUID tenantId, UUID integrationId, String accountId,
                             String reason, String triggeredBy) {
        RateLimitResult result = rateLimitService.checkAndRecord(tenantId);
        if (!result.allowed()) {
            throw new DomainError.ValidationError("INGEST_429",
                "Rate limit exceeded. Retry after " + result.retryAfterSeconds() + " seconds.");
        }

        Instant now = Instant.now();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        DateWindow dateWindow = new DateWindow(today.minusDays(3), today.minusDays(1));

        SyncJob job = SyncJob.create(
            UUID.randomUUID(), integrationId, tenantId, accountId, REPORT_TYPE,
            dateWindow, TriggerType.FORCE_RUN, triggeredBy, reason, now
        );
        SyncJob saved = syncJobRepository.save(job);
        publishAndClear(saved);
        return saved;
    }

    public SyncIncident acknowledgeEscalation(UUID incidentId, String ackReason, String by) {
        SyncIncident incident = incidentRepository.findById(incidentId)
            .orElseThrow(() -> new DomainError.NotFound("INC_404",
                "SyncIncident not found: " + incidentId));
        Instant now = Instant.now();
        incident.acknowledge(ackReason, by, now);
        SyncIncident saved = incidentRepository.save(incident);
        publishAndClear(saved);
        return saved;
    }

    private void publishAndClear(SyncJob job) {
        job.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        job.clearEvents();
    }

    private void publishAndClear(SyncIncident incident) {
        incident.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        incident.clearEvents();
    }
}
