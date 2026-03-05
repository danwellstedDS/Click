package com.derbysoft.click.modules.ingestion.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "sync_jobs")
public class SyncJobEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "integration_id", nullable = false, updatable = false)
    private UUID integrationId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "account_id", nullable = false, length = 20, updatable = false)
    private String accountId;

    @Column(name = "report_type", nullable = false, length = 50, updatable = false)
    private String reportType;

    @Column(name = "date_from", nullable = false, updatable = false)
    private LocalDate dateFrom;

    @Column(name = "date_to", nullable = false, updatable = false)
    private LocalDate dateTo;

    @Column(name = "trigger_type", nullable = false, length = 20, updatable = false)
    private String triggerType;

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

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "triggered_by", length = 255)
    private String triggeredBy;

    @Column(name = "trigger_reason")
    private String triggerReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SyncJobEntity() {}

    public SyncJobEntity(UUID id, UUID integrationId, UUID tenantId, String accountId,
                          String reportType, LocalDate dateFrom, LocalDate dateTo,
                          String triggerType, String idempotencyKey,
                          String status, int attempts, int maxAttempts,
                          Instant lastAttemptAt, Instant leaseExpiresAt, Instant nextAttemptAfter,
                          String failureClass, String failureReason,
                          String triggeredBy, String triggerReason) {
        this.id = id;
        this.integrationId = integrationId;
        this.tenantId = tenantId;
        this.accountId = accountId;
        this.reportType = reportType;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
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
    }

    public UUID getId() { return id; }
    public UUID getIntegrationId() { return integrationId; }
    public UUID getTenantId() { return tenantId; }
    public String getAccountId() { return accountId; }
    public String getReportType() { return reportType; }
    public LocalDate getDateFrom() { return dateFrom; }
    public LocalDate getDateTo() { return dateTo; }
    public String getTriggerType() { return triggerType; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getStatus() { return status; }
    public int getAttempts() { return attempts; }
    public int getMaxAttempts() { return maxAttempts; }
    public Instant getLastAttemptAt() { return lastAttemptAt; }
    public Instant getLeaseExpiresAt() { return leaseExpiresAt; }
    public Instant getNextAttemptAfter() { return nextAttemptAfter; }
    public String getFailureClass() { return failureClass; }
    public String getFailureReason() { return failureReason; }
    public String getTriggeredBy() { return triggeredBy; }
    public String getTriggerReason() { return triggerReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(String status) { this.status = status; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public void setLastAttemptAt(Instant lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }
    public void setLeaseExpiresAt(Instant leaseExpiresAt) { this.leaseExpiresAt = leaseExpiresAt; }
    public void setNextAttemptAfter(Instant nextAttemptAfter) { this.nextAttemptAfter = nextAttemptAfter; }
    public void setFailureClass(String failureClass) { this.failureClass = failureClass; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
