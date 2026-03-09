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
@Table(name = "write_actions")
public class WriteActionEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "revision_id", nullable = false, updatable = false)
    private UUID revisionId;

    @Column(name = "item_id", nullable = false, updatable = false)
    private UUID itemId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "action_type", nullable = false, length = 30, updatable = false)
    private String actionType;

    @Column(name = "idempotency_key", nullable = false, length = 255, updatable = false)
    private String idempotencyKey;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "lease_expires_at")
    private Instant leaseExpiresAt;

    @Column(name = "next_attempt_after")
    private Instant nextAttemptAfter;

    @Column(name = "failure_class", length = 20)
    private String failureClass;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "triggered_by", length = 255)
    private String triggeredBy;

    @Column(name = "trigger_type", nullable = false, length = 20, updatable = false)
    private String triggerType;

    @Column(name = "trigger_reason", columnDefinition = "TEXT")
    private String triggerReason;

    @Column(name = "target_customer_id", length = 30)
    private String targetCustomerId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected WriteActionEntity() {}

    public WriteActionEntity(UUID id, UUID revisionId, UUID itemId, UUID tenantId,
                              String actionType, String idempotencyKey,
                              String status, int attempts, int maxAttempts,
                              Instant lastAttemptAt, Instant leaseExpiresAt, Instant nextAttemptAfter,
                              String failureClass, String failureReason,
                              String targetCustomerId,
                              String triggeredBy, String triggerType, String triggerReason) {
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
        this.targetCustomerId = targetCustomerId;
        this.triggeredBy = triggeredBy;
        this.triggerType = triggerType;
        this.triggerReason = triggerReason;
    }

    public UUID getId() { return id; }
    public UUID getRevisionId() { return revisionId; }
    public UUID getItemId() { return itemId; }
    public UUID getTenantId() { return tenantId; }
    public String getActionType() { return actionType; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getStatus() { return status; }
    public int getAttempts() { return attempts; }
    public int getMaxAttempts() { return maxAttempts; }
    public Instant getLastAttemptAt() { return lastAttemptAt; }
    public Instant getLeaseExpiresAt() { return leaseExpiresAt; }
    public Instant getNextAttemptAfter() { return nextAttemptAfter; }
    public String getFailureClass() { return failureClass; }
    public String getFailureReason() { return failureReason; }
    public String getTargetCustomerId() { return targetCustomerId; }
    public String getTriggeredBy() { return triggeredBy; }
    public String getTriggerType() { return triggerType; }
    public String getTriggerReason() { return triggerReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(String status) { this.status = status; }
    public void setTargetCustomerId(String targetCustomerId) { this.targetCustomerId = targetCustomerId; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public void setLastAttemptAt(Instant lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }
    public void setLeaseExpiresAt(Instant leaseExpiresAt) { this.leaseExpiresAt = leaseExpiresAt; }
    public void setNextAttemptAfter(Instant nextAttemptAfter) { this.nextAttemptAfter = nextAttemptAfter; }
    public void setFailureClass(String failureClass) { this.failureClass = failureClass; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
