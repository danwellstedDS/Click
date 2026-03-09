package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "plan_items")
public class PlanItemEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "revision_id", nullable = false, updatable = false)
    private UUID revisionId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "action_type", nullable = false, length = 30, updatable = false)
    private String actionType;

    @Column(name = "resource_type", nullable = false, length = 30, updatable = false)
    private String resourceType;

    @Column(name = "resource_id", length = 255)
    private String resourceId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "apply_order", nullable = false, updatable = false)
    private int applyOrder;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "next_attempt_after")
    private Instant nextAttemptAfter;

    @Column(name = "failure_class", length = 20)
    private String failureClass;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PlanItemEntity() {}

    public PlanItemEntity(UUID id, UUID revisionId, UUID tenantId, String status,
                           String actionType, String resourceType, String resourceId,
                           String payload, int applyOrder,
                           int attempts, int maxAttempts,
                           Instant lastAttemptAt, Instant nextAttemptAfter,
                           String failureClass, String failureReason) {
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
    }

    public UUID getId() { return id; }
    public UUID getRevisionId() { return revisionId; }
    public UUID getTenantId() { return tenantId; }
    public String getStatus() { return status; }
    public String getActionType() { return actionType; }
    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
    public String getPayload() { return payload; }
    public int getApplyOrder() { return applyOrder; }
    public int getAttempts() { return attempts; }
    public int getMaxAttempts() { return maxAttempts; }
    public Instant getLastAttemptAt() { return lastAttemptAt; }
    public Instant getNextAttemptAfter() { return nextAttemptAfter; }
    public String getFailureClass() { return failureClass; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(String status) { this.status = status; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public void setLastAttemptAt(Instant lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }
    public void setNextAttemptAfter(Instant nextAttemptAfter) { this.nextAttemptAfter = nextAttemptAfter; }
    public void setFailureClass(String failureClass) { this.failureClass = failureClass; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
