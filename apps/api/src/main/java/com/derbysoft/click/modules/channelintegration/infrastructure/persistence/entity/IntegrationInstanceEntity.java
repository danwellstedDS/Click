package com.derbysoft.click.modules.channelintegration.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "integration_instances",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_tenant_channel_connection",
        columnNames = {"tenant_id", "channel", "connection_key"}
    )
)
public class IntegrationInstanceEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 50, updatable = false)
    private String channel;

    @Column(name = "connection_key", nullable = false, length = 100, updatable = false)
    private String connectionKey;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "credential_ref_id")
    private UUID credentialRefId;

    @Column(name = "cadence_type", nullable = false, length = 20)
    private String cadenceType;

    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    @Column(name = "interval_minutes")
    private Integer intervalMinutes;

    @Column(name = "schedule_timezone", nullable = false, length = 100)
    private String scheduleTimezone;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    @Column(name = "last_sync_status", nullable = false, length = 20)
    private String lastSyncStatus;

    @Column(name = "last_success_at")
    private Instant lastSuccessAt;

    @Column(name = "last_error_code", length = 50)
    private String lastErrorCode;

    @Column(name = "last_error_message")
    private String lastErrorMessage;

    @Column(name = "consecutive_failures", nullable = false)
    private int consecutiveFailures;

    @Column(name = "status_reason")
    private String statusReason;

    @Column(name = "credential_attached_at")
    private Instant credentialAttachedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected IntegrationInstanceEntity() {}

    public IntegrationInstanceEntity(
        UUID id,
        UUID tenantId,
        String channel,
        String connectionKey,
        String status,
        UUID credentialRefId,
        String cadenceType,
        String cronExpression,
        Integer intervalMinutes,
        String scheduleTimezone,
        Instant lastSyncAt,
        String lastSyncStatus,
        Instant lastSuccessAt,
        String lastErrorCode,
        String lastErrorMessage,
        int consecutiveFailures,
        String statusReason,
        Instant credentialAttachedAt,
        UUID updatedBy
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.channel = channel;
        this.connectionKey = connectionKey;
        this.status = status;
        this.credentialRefId = credentialRefId;
        this.cadenceType = cadenceType;
        this.cronExpression = cronExpression;
        this.intervalMinutes = intervalMinutes;
        this.scheduleTimezone = scheduleTimezone;
        this.lastSyncAt = lastSyncAt;
        this.lastSyncStatus = lastSyncStatus;
        this.lastSuccessAt = lastSuccessAt;
        this.lastErrorCode = lastErrorCode;
        this.lastErrorMessage = lastErrorMessage;
        this.consecutiveFailures = consecutiveFailures;
        this.statusReason = statusReason;
        this.credentialAttachedAt = credentialAttachedAt;
        this.updatedBy = updatedBy;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getChannel() { return channel; }
    public String getConnectionKey() { return connectionKey; }
    public String getStatus() { return status; }
    public UUID getCredentialRefId() { return credentialRefId; }
    public String getCadenceType() { return cadenceType; }
    public String getCronExpression() { return cronExpression; }
    public Integer getIntervalMinutes() { return intervalMinutes; }
    public String getScheduleTimezone() { return scheduleTimezone; }
    public Instant getLastSyncAt() { return lastSyncAt; }
    public String getLastSyncStatus() { return lastSyncStatus; }
    public Instant getLastSuccessAt() { return lastSuccessAt; }
    public String getLastErrorCode() { return lastErrorCode; }
    public String getLastErrorMessage() { return lastErrorMessage; }
    public int getConsecutiveFailures() { return consecutiveFailures; }
    public String getStatusReason() { return statusReason; }
    public Instant getCredentialAttachedAt() { return credentialAttachedAt; }
    public UUID getUpdatedBy() { return updatedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(String status) { this.status = status; }
    public void setCredentialRefId(UUID credentialRefId) { this.credentialRefId = credentialRefId; }
    public void setCadenceType(String cadenceType) { this.cadenceType = cadenceType; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public void setIntervalMinutes(Integer intervalMinutes) { this.intervalMinutes = intervalMinutes; }
    public void setScheduleTimezone(String scheduleTimezone) { this.scheduleTimezone = scheduleTimezone; }
    public void setLastSyncAt(Instant lastSyncAt) { this.lastSyncAt = lastSyncAt; }
    public void setLastSyncStatus(String lastSyncStatus) { this.lastSyncStatus = lastSyncStatus; }
    public void setLastSuccessAt(Instant lastSuccessAt) { this.lastSuccessAt = lastSuccessAt; }
    public void setLastErrorCode(String lastErrorCode) { this.lastErrorCode = lastErrorCode; }
    public void setLastErrorMessage(String lastErrorMessage) { this.lastErrorMessage = lastErrorMessage; }
    public void setConsecutiveFailures(int consecutiveFailures) { this.consecutiveFailures = consecutiveFailures; }
    public void setStatusReason(String statusReason) { this.statusReason = statusReason; }
    public void setCredentialAttachedAt(Instant credentialAttachedAt) { this.credentialAttachedAt = credentialAttachedAt; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }
}
