package com.derbysoft.click.modules.ingestion.domain.aggregates;

import com.derbysoft.click.modules.ingestion.domain.events.SyncFailed;
import com.derbysoft.click.modules.ingestion.domain.events.SyncStarted;
import com.derbysoft.click.modules.ingestion.domain.events.SyncSucceeded;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.DateWindow;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.SyncJobStatus;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.TriggerType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class SyncJob {

    private final UUID id;
    private final UUID integrationId;
    private final UUID tenantId;
    private final String accountId;
    private final String reportType;
    private final DateWindow dateWindow;
    private final TriggerType triggerType;
    private final String idempotencyKey;
    private SyncJobStatus status;
    private int attempts;
    private final int maxAttempts;
    private Instant lastAttemptAt;
    private Instant leaseExpiresAt;
    private Instant nextAttemptAfter;
    private FailureClass failureClass;
    private String failureReason;
    private final String triggeredBy;
    private final String triggerReason;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<Object> events = new ArrayList<>();

    private SyncJob(
        UUID id, UUID integrationId, UUID tenantId, String accountId, String reportType,
        DateWindow dateWindow, TriggerType triggerType, String idempotencyKey,
        SyncJobStatus status, int attempts, int maxAttempts,
        Instant lastAttemptAt, Instant leaseExpiresAt, Instant nextAttemptAfter,
        FailureClass failureClass, String failureReason,
        String triggeredBy, String triggerReason,
        Instant createdAt, Instant updatedAt
    ) {
        this.id = id;
        this.integrationId = integrationId;
        this.tenantId = tenantId;
        this.accountId = accountId;
        this.reportType = reportType;
        this.dateWindow = dateWindow;
        this.triggerType = triggerType;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
        this.attempts = attempts;
        this.maxAttempts = maxAttempts;
        this.lastAttemptAt = lastAttemptAt;
        this.leaseExpiresAt = leaseExpiresAt;
        this.nextAttemptAfter = nextAttemptAfter;
        this.failureClass = failureClass;
        this.failureReason = failureReason;
        this.triggeredBy = triggeredBy;
        this.triggerReason = triggerReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SyncJob create(
        UUID id, UUID integrationId, UUID tenantId, String accountId, String reportType,
        DateWindow dateWindow, TriggerType triggerType, String triggeredBy, String triggerReason,
        Instant now
    ) {
        String idempotencyKey = integrationId + ":" + accountId + ":"
            + dateWindow.from() + ":" + dateWindow.to() + ":" + reportType;
        SyncJob job = new SyncJob(
            id, integrationId, tenantId, accountId, reportType,
            dateWindow, triggerType, idempotencyKey,
            SyncJobStatus.PENDING, 0, 5,
            null, null, null,
            null, null,
            triggeredBy, triggerReason,
            now, now
        );
        job.events.add(new SyncStarted(id, idempotencyKey, tenantId, triggerType, now));
        return job;
    }

    public static SyncJob reconstitute(
        UUID id, UUID integrationId, UUID tenantId, String accountId, String reportType,
        DateWindow dateWindow, TriggerType triggerType, String idempotencyKey,
        SyncJobStatus status, int attempts, int maxAttempts,
        Instant lastAttemptAt, Instant leaseExpiresAt, Instant nextAttemptAfter,
        FailureClass failureClass, String failureReason,
        String triggeredBy, String triggerReason,
        Instant createdAt, Instant updatedAt
    ) {
        return new SyncJob(
            id, integrationId, tenantId, accountId, reportType,
            dateWindow, triggerType, idempotencyKey,
            status, attempts, maxAttempts,
            lastAttemptAt, leaseExpiresAt, nextAttemptAfter,
            failureClass, failureReason,
            triggeredBy, triggerReason,
            createdAt, updatedAt
        );
    }

    public void acquireLease(Instant now) {
        this.status = SyncJobStatus.RUNNING;
        this.leaseExpiresAt = now.plusSeconds(3600);
        this.attempts++;
        this.lastAttemptAt = now;
        this.updatedAt = now;
    }

    public void markSucceeded(Instant now) {
        this.status = SyncJobStatus.SUCCEEDED;
        this.leaseExpiresAt = null;
        this.updatedAt = now;
        events.add(new SyncSucceeded(id, idempotencyKey, tenantId, now));
    }

    public void markFailed(FailureClass failureClass, String reason, Instant now) {
        this.status = SyncJobStatus.FAILED;
        this.failureClass = failureClass;
        this.failureReason = reason;
        this.leaseExpiresAt = null;
        this.updatedAt = now;
        events.add(new SyncFailed(id, idempotencyKey, tenantId, failureClass, reason, now));
    }

    public void markStuck(Instant now) {
        this.status = SyncJobStatus.STUCK;
        this.failureClass = FailureClass.TRANSIENT;
        this.failureReason = "stuck";
        this.leaseExpiresAt = null;
        this.updatedAt = now;
        events.add(new SyncFailed(id, idempotencyKey, tenantId, FailureClass.TRANSIENT, "stuck", now));
    }

    public void requeueForRetry(Instant nextAttemptAfter, Instant now) {
        this.status = SyncJobStatus.PENDING;
        this.nextAttemptAfter = nextAttemptAfter;
        this.leaseExpiresAt = null;
        this.updatedAt = now;
    }

    public boolean canRetry() {
        return attempts < maxAttempts && failureClass == FailureClass.TRANSIENT;
    }

    public UUID getId() { return id; }
    public UUID getIntegrationId() { return integrationId; }
    public UUID getTenantId() { return tenantId; }
    public String getAccountId() { return accountId; }
    public String getReportType() { return reportType; }
    public DateWindow getDateWindow() { return dateWindow; }
    public TriggerType getTriggerType() { return triggerType; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public SyncJobStatus getStatus() { return status; }
    public int getAttempts() { return attempts; }
    public int getMaxAttempts() { return maxAttempts; }
    public Instant getLastAttemptAt() { return lastAttemptAt; }
    public Instant getLeaseExpiresAt() { return leaseExpiresAt; }
    public Instant getNextAttemptAfter() { return nextAttemptAfter; }
    public FailureClass getFailureClass() { return failureClass; }
    public String getFailureReason() { return failureReason; }
    public String getTriggeredBy() { return triggeredBy; }
    public String getTriggerReason() { return triggerReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<Object> getEvents() { return Collections.unmodifiableList(events); }
    public void clearEvents() { events.clear(); }
}
