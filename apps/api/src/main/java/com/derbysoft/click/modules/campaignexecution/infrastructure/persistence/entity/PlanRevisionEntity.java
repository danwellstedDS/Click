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
@Table(name = "plan_revisions")
public class PlanRevisionEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "plan_id", nullable = false, updatable = false)
    private UUID planId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "revision_number", nullable = false, updatable = false)
    private int revisionNumber;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "published_by", length = 255)
    private String publishedBy;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "cancelled_by", length = 255)
    private String cancelledBy;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PlanRevisionEntity() {}

    public PlanRevisionEntity(UUID id, UUID planId, UUID tenantId, int revisionNumber,
                               String status, String publishedBy, Instant publishedAt,
                               String cancelledBy, String cancelReason, Instant cancelledAt) {
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
    }

    public UUID getId() { return id; }
    public UUID getPlanId() { return planId; }
    public UUID getTenantId() { return tenantId; }
    public int getRevisionNumber() { return revisionNumber; }
    public String getStatus() { return status; }
    public String getPublishedBy() { return publishedBy; }
    public Instant getPublishedAt() { return publishedAt; }
    public String getCancelledBy() { return cancelledBy; }
    public String getCancelReason() { return cancelReason; }
    public Instant getCancelledAt() { return cancelledAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(String status) { this.status = status; }
    public void setPublishedBy(String publishedBy) { this.publishedBy = publishedBy; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
}
