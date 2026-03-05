package com.derbysoft.click.modules.ingestion.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "sync_incidents")
public class SyncIncidentEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, length = 255, updatable = false)
    private String idempotencyKey;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "failure_class", nullable = false, length = 20, updatable = false)
    private String failureClass;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "consecutive_failures", nullable = false)
    private int consecutiveFailures;

    @Column(name = "first_failed_at", nullable = false, updatable = false)
    private Instant firstFailedAt;

    @Column(name = "last_failed_at", nullable = false)
    private Instant lastFailedAt;

    @Column(name = "acknowledged_by", length = 255)
    private String acknowledgedBy;

    @Column(name = "ack_reason")
    private String ackReason;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SyncIncidentEntity() {}

    public SyncIncidentEntity(UUID id, String idempotencyKey, UUID tenantId, String failureClass,
                               String status, int consecutiveFailures,
                               Instant firstFailedAt, Instant lastFailedAt,
                               String acknowledgedBy, String ackReason, Instant acknowledgedAt) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.tenantId = tenantId;
        this.failureClass = failureClass;
        this.status = status;
        this.consecutiveFailures = consecutiveFailures;
        this.firstFailedAt = firstFailedAt;
        this.lastFailedAt = lastFailedAt;
        this.acknowledgedBy = acknowledgedBy;
        this.ackReason = ackReason;
        this.acknowledgedAt = acknowledgedAt;
    }

    public UUID getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public UUID getTenantId() { return tenantId; }
    public String getFailureClass() { return failureClass; }
    public String getStatus() { return status; }
    public int getConsecutiveFailures() { return consecutiveFailures; }
    public Instant getFirstFailedAt() { return firstFailedAt; }
    public Instant getLastFailedAt() { return lastFailedAt; }
    public String getAcknowledgedBy() { return acknowledgedBy; }
    public String getAckReason() { return ackReason; }
    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(String status) { this.status = status; }
    public void setConsecutiveFailures(int consecutiveFailures) { this.consecutiveFailures = consecutiveFailures; }
    public void setLastFailedAt(Instant lastFailedAt) { this.lastFailedAt = lastFailedAt; }
    public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }
    public void setAckReason(String ackReason) { this.ackReason = ackReason; }
    public void setAcknowledgedAt(Instant acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
}
