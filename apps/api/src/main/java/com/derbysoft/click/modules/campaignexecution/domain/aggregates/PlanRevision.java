package com.derbysoft.click.modules.campaignexecution.domain.aggregates;

import com.derbysoft.click.modules.campaignexecution.domain.events.PlanRevisionApplyCompleted;
import com.derbysoft.click.modules.campaignexecution.domain.events.PlanRevisionApplyStarted;
import com.derbysoft.click.modules.campaignexecution.domain.events.PlanRevisionCancelled;
import com.derbysoft.click.modules.campaignexecution.domain.events.PlanRevisionPublished;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.PlanRevisionStatus;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class PlanRevision {

    private final UUID id;
    private final UUID planId;
    private final UUID tenantId;
    private final int revisionNumber;
    private PlanRevisionStatus status;
    private String publishedBy;
    private Instant publishedAt;
    private String cancelledBy;
    private String cancelReason;
    private Instant cancelledAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<Object> events = new ArrayList<>();

    private PlanRevision(UUID id, UUID planId, UUID tenantId, int revisionNumber,
                          PlanRevisionStatus status,
                          String publishedBy, Instant publishedAt,
                          String cancelledBy, String cancelReason, Instant cancelledAt,
                          Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.planId = planId;
        this.tenantId = tenantId;
        this.revisionNumber = revisionNumber;
        this.status = status;
        this.publishedBy = publishedBy;
        this.publishedAt = publishedAt;
        this.cancelledBy = cancelledBy;
        this.cancelReason = cancelReason;
        this.cancelledAt = cancelledAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PlanRevision create(UUID id, UUID planId, UUID tenantId, int revisionNumber,
                                       Instant now) {
        return new PlanRevision(id, planId, tenantId, revisionNumber,
            PlanRevisionStatus.DRAFT,
            null, null, null, null, null,
            now, now);
    }

    public static PlanRevision reconstitute(UUID id, UUID planId, UUID tenantId, int revisionNumber,
                                             PlanRevisionStatus status,
                                             String publishedBy, Instant publishedAt,
                                             String cancelledBy, String cancelReason, Instant cancelledAt,
                                             Instant createdAt, Instant updatedAt) {
        return new PlanRevision(id, planId, tenantId, revisionNumber, status,
            publishedBy, publishedAt,
            cancelledBy, cancelReason, cancelledAt,
            createdAt, updatedAt);
    }

    public void publish(String publishedBy, Instant now) {
        if (status != PlanRevisionStatus.DRAFT) {
            throw new DomainError.Conflict("REV_409",
                "Can only publish DRAFT revisions; current status: " + status);
        }
        this.status = PlanRevisionStatus.PUBLISHED;
        this.publishedBy = publishedBy;
        this.publishedAt = now;
        this.updatedAt = now;
        events.add(new PlanRevisionPublished(id, planId, tenantId, publishedBy, now));
    }

    public void startApply(Instant now) {
        if (status != PlanRevisionStatus.PUBLISHED) {
            throw new DomainError.Conflict("REV_409",
                "Can only apply PUBLISHED revisions; current status: " + status);
        }
        this.status = PlanRevisionStatus.APPLYING;
        this.updatedAt = now;
        events.add(new PlanRevisionApplyStarted(id, planId, tenantId, now));
    }

    public void completeApply(int succeededCount, int failedCount, Instant now) {
        this.status = failedCount > 0 && succeededCount == 0
            ? PlanRevisionStatus.FAILED
            : PlanRevisionStatus.APPLIED;
        this.updatedAt = now;
        events.add(new PlanRevisionApplyCompleted(id, planId, tenantId, succeededCount, failedCount, now));
    }

    public void cancel(String by, String reason, Instant now) {
        if (status == PlanRevisionStatus.APPLYING || status == PlanRevisionStatus.APPLIED) {
            throw new DomainError.Conflict("REV_409",
                "Cannot cancel a revision in status: " + status);
        }
        this.status = PlanRevisionStatus.CANCELLED;
        this.cancelledBy = by;
        this.cancelReason = reason;
        this.cancelledAt = now;
        this.updatedAt = now;
        events.add(new PlanRevisionCancelled(id, planId, tenantId, by, reason, now));
    }

    public UUID getId() { return id; }
    public UUID getPlanId() { return planId; }
    public UUID getTenantId() { return tenantId; }
    public int getRevisionNumber() { return revisionNumber; }
    public PlanRevisionStatus getStatus() { return status; }
    public String getPublishedBy() { return publishedBy; }
    public Instant getPublishedAt() { return publishedAt; }
    public String getCancelledBy() { return cancelledBy; }
    public String getCancelReason() { return cancelReason; }
    public Instant getCancelledAt() { return cancelledAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<Object> getEvents() { return Collections.unmodifiableList(events); }
    public void clearEvents() { events.clear(); }
}
