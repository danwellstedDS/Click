package com.derbysoft.click.modules.campaignexecution.domain.aggregates;

import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.IdempotencyKey;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.TriggerType;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionStatus;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class WriteAction {

    private final UUID id;
    private final UUID revisionId;
    private final UUID itemId;
    private final UUID tenantId;
    private final WriteActionType actionType;
    private final String idempotencyKey;
    private WriteActionStatus status;
    private int attempts;
    private final int maxAttempts;
    private Instant lastAttemptAt;
    private Instant leaseExpiresAt;
    private Instant nextAttemptAfter;
    private FailureClass failureClass;
    private String failureReason;
    private final String triggeredBy;
    private final TriggerType triggerType;
    private final String triggerReason;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<Object> events = new ArrayList<>();

    private WriteAction(UUID id, UUID revisionId, UUID itemId, UUID tenantId,
                        WriteActionType actionType, String idempotencyKey,
                        WriteActionStatus status, int attempts, int maxAttempts,
                        Instant lastAttemptAt, Instant leaseExpiresAt, Instant nextAttemptAfter,
                        FailureClass failureClass, String failureReason,
                        String triggeredBy, TriggerType triggerType, String triggerReason,
                        Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.revisionId = revisionId;
        this.itemId = itemId;
        this.tenantId = tenantId;
        this.actionType = actionType;
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
        this.triggerType = triggerType;
        this.triggerReason = triggerReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static WriteAction create(UUID id, UUID revisionId, UUID itemId, UUID tenantId,
                                      WriteActionType actionType, int targetVersion,
                                      TriggerType triggerType,
                                      String triggeredBy, String triggerReason, Instant now) {
        String key = IdempotencyKey.compute(revisionId, itemId, actionType, targetVersion);
        return new WriteAction(id, revisionId, itemId, tenantId, actionType, key,
            WriteActionStatus.PENDING, 0, 3,
            null, null, null,
            null, null,
            triggeredBy, triggerType, triggerReason,
            now, now);
    }

    public static WriteAction reconstitute(UUID id, UUID revisionId, UUID itemId, UUID tenantId,
                                            WriteActionType actionType, String idempotencyKey,
                                            WriteActionStatus status, int attempts, int maxAttempts,
                                            Instant lastAttemptAt, Instant leaseExpiresAt,
                                            Instant nextAttemptAfter,
                                            FailureClass failureClass, String failureReason,
                                            String triggeredBy, TriggerType triggerType,
                                            String triggerReason,
                                            Instant createdAt, Instant updatedAt) {
        return new WriteAction(id, revisionId, itemId, tenantId, actionType, idempotencyKey,
            status, attempts, maxAttempts,
            lastAttemptAt, leaseExpiresAt, nextAttemptAfter,
            failureClass, failureReason,
            triggeredBy, triggerType, triggerReason,
            createdAt, updatedAt);
    }

    public void acquireLease(Instant now) {
        this.status = WriteActionStatus.RUNNING;
        this.leaseExpiresAt = now.plusSeconds(600);
        this.attempts++;
        this.lastAttemptAt = now;
        this.updatedAt = now;
    }

    public void markSucceeded(Instant now) {
        this.status = WriteActionStatus.SUCCEEDED;
        this.leaseExpiresAt = null;
        this.updatedAt = now;
    }

    public void markFailed(FailureClass failureClass, String reason, Instant now) {
        this.status = WriteActionStatus.FAILED;
        this.failureClass = failureClass;
        this.failureReason = reason;
        this.leaseExpiresAt = null;
        this.updatedAt = now;
    }

    public void requeueForRetry(Instant nextAttemptAfter, Instant now) {
        this.status = WriteActionStatus.PENDING;
        this.nextAttemptAfter = nextAttemptAfter;
        this.leaseExpiresAt = null;
        this.updatedAt = now;
    }

    public void cancel(Instant now) {
        this.status = WriteActionStatus.CANCELLED;
        this.leaseExpiresAt = null;
        this.updatedAt = now;
    }

    public boolean canRetry() {
        return attempts < maxAttempts && failureClass == FailureClass.TRANSIENT;
    }

    public UUID getId() { return id; }
    public UUID getRevisionId() { return revisionId; }
    public UUID getItemId() { return itemId; }
    public UUID getTenantId() { return tenantId; }
    public WriteActionType getActionType() { return actionType; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public WriteActionStatus getStatus() { return status; }
    public int getAttempts() { return attempts; }
    public int getMaxAttempts() { return maxAttempts; }
    public Instant getLastAttemptAt() { return lastAttemptAt; }
    public Instant getLeaseExpiresAt() { return leaseExpiresAt; }
    public Instant getNextAttemptAfter() { return nextAttemptAfter; }
    public FailureClass getFailureClass() { return failureClass; }
    public String getFailureReason() { return failureReason; }
    public String getTriggeredBy() { return triggeredBy; }
    public TriggerType getTriggerType() { return triggerType; }
    public String getTriggerReason() { return triggerReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<Object> getEvents() { return Collections.unmodifiableList(events); }
    public void clearEvents() { events.clear(); }
}
