package com.derbysoft.click.modules.campaignexecution.domain.entities;

import com.derbysoft.click.modules.campaignexecution.domain.events.WriteActionBlocked;
import com.derbysoft.click.modules.campaignexecution.domain.events.WriteActionFailed;
import com.derbysoft.click.modules.campaignexecution.domain.events.WriteActionQueued;
import com.derbysoft.click.modules.campaignexecution.domain.events.WriteActionStarted;
import com.derbysoft.click.modules.campaignexecution.domain.events.WriteActionSucceeded;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.ApplyOrder;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.PlanItemStatus;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class PlanItem {

    private final UUID id;
    private final UUID revisionId;
    private final UUID tenantId;
    private PlanItemStatus status;
    private final WriteActionType actionType;
    private final String resourceType;
    private String resourceId;
    private final String payload;
    private final ApplyOrder applyOrder;
    private int attempts;
    private final int maxAttempts;
    private Instant lastAttemptAt;
    private Instant nextAttemptAfter;
    private FailureClass failureClass;
    private String failureReason;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<Object> events = new ArrayList<>();

    private PlanItem(UUID id, UUID revisionId, UUID tenantId, PlanItemStatus status,
                     WriteActionType actionType, String resourceType, String resourceId,
                     String payload, ApplyOrder applyOrder,
                     int attempts, int maxAttempts,
                     Instant lastAttemptAt, Instant nextAttemptAfter,
                     FailureClass failureClass, String failureReason,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.revisionId = revisionId;
        this.tenantId = tenantId;
        this.status = status;
        this.actionType = actionType;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.payload = payload;
        this.applyOrder = applyOrder;
        this.attempts = attempts;
        this.maxAttempts = maxAttempts;
        this.lastAttemptAt = lastAttemptAt;
        this.nextAttemptAfter = nextAttemptAfter;
        this.failureClass = failureClass;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PlanItem create(UUID id, UUID revisionId, UUID tenantId,
                                   WriteActionType actionType, String resourceType,
                                   String resourceId, String payload, ApplyOrder applyOrder,
                                   Instant now) {
        return new PlanItem(id, revisionId, tenantId, PlanItemStatus.DRAFT,
            actionType, resourceType, resourceId, payload, applyOrder,
            0, 3, null, null, null, null, now, now);
    }

    public static PlanItem reconstitute(UUID id, UUID revisionId, UUID tenantId,
                                         PlanItemStatus status,
                                         WriteActionType actionType, String resourceType,
                                         String resourceId, String payload, ApplyOrder applyOrder,
                                         int attempts, int maxAttempts,
                                         Instant lastAttemptAt, Instant nextAttemptAfter,
                                         FailureClass failureClass, String failureReason,
                                         Instant createdAt, Instant updatedAt) {
        return new PlanItem(id, revisionId, tenantId, status,
            actionType, resourceType, resourceId, payload, applyOrder,
            attempts, maxAttempts, lastAttemptAt, nextAttemptAfter,
            failureClass, failureReason, createdAt, updatedAt);
    }

    public void publish() {
        this.status = PlanItemStatus.PUBLISHED;
    }

    public void enqueue(Instant now) {
        this.status = PlanItemStatus.QUEUED;
        this.updatedAt = now;
        events.add(new WriteActionQueued(id, revisionId, tenantId, actionType, now));
    }

    public void startExecution(Instant now) {
        this.status = PlanItemStatus.IN_PROGRESS;
        this.attempts++;
        this.lastAttemptAt = now;
        this.updatedAt = now;
        events.add(new WriteActionStarted(id, revisionId, tenantId, actionType, now));
    }

    public void markSucceeded(String resourceId, Instant now) {
        this.status = PlanItemStatus.SUCCEEDED;
        this.resourceId = resourceId;
        this.updatedAt = now;
        events.add(new WriteActionSucceeded(id, revisionId, tenantId, actionType, resourceId, now));
    }

    public void markFailed(FailureClass fc, String reason, Instant now) {
        this.status = PlanItemStatus.FAILED;
        this.failureClass = fc;
        this.failureReason = reason;
        this.updatedAt = now;
        events.add(new WriteActionFailed(id, revisionId, tenantId, actionType, fc, reason, now));
    }

    public void block(String reason, Instant now) {
        this.status = PlanItemStatus.BLOCKED;
        this.failureReason = reason;
        this.updatedAt = now;
        events.add(new WriteActionBlocked(id, revisionId, tenantId, reason, now));
    }

    public void cancel(Instant now) {
        this.status = PlanItemStatus.CANCELLED;
        this.updatedAt = now;
    }

    public void requeueForRetry(Instant nextAttemptAfter, Instant now) {
        this.status = PlanItemStatus.QUEUED;
        this.nextAttemptAfter = nextAttemptAfter;
        this.updatedAt = now;
    }

    public boolean canRetry() {
        return attempts < maxAttempts && failureClass == FailureClass.TRANSIENT;
    }

    public UUID getId() { return id; }
    public UUID getRevisionId() { return revisionId; }
    public UUID getTenantId() { return tenantId; }
    public PlanItemStatus getStatus() { return status; }
    public WriteActionType getActionType() { return actionType; }
    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
    public String getPayload() { return payload; }
    public ApplyOrder getApplyOrder() { return applyOrder; }
    public int getAttempts() { return attempts; }
    public int getMaxAttempts() { return maxAttempts; }
    public Instant getLastAttemptAt() { return lastAttemptAt; }
    public Instant getNextAttemptAfter() { return nextAttemptAfter; }
    public FailureClass getFailureClass() { return failureClass; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<Object> getEvents() { return Collections.unmodifiableList(events); }
    public void clearEvents() { events.clear(); }
}
