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
        name = "uq_tenant_channel",
        columnNames = {"tenant_id", "channel"}
    )
)
public class IntegrationInstanceEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 50)
    private String channel;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "credential_ref_id")
    private UUID credentialRefId;

    @Column(name = "sync_schedule_cron", nullable = false, length = 100)
    private String syncScheduleCron;

    @Column(name = "sync_schedule_timezone", nullable = false, length = 100)
    private String syncScheduleTimezone;

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
        String status,
        UUID credentialRefId,
        String syncScheduleCron,
        String syncScheduleTimezone
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.channel = channel;
        this.status = status;
        this.credentialRefId = credentialRefId;
        this.syncScheduleCron = syncScheduleCron;
        this.syncScheduleTimezone = syncScheduleTimezone;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getChannel() { return channel; }
    public String getStatus() { return status; }
    public UUID getCredentialRefId() { return credentialRefId; }
    public String getSyncScheduleCron() { return syncScheduleCron; }
    public String getSyncScheduleTimezone() { return syncScheduleTimezone; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(String status) { this.status = status; }
    public void setCredentialRefId(UUID credentialRefId) { this.credentialRefId = credentialRefId; }
    public void setSyncScheduleCron(String syncScheduleCron) { this.syncScheduleCron = syncScheduleCron; }
    public void setSyncScheduleTimezone(String syncScheduleTimezone) { this.syncScheduleTimezone = syncScheduleTimezone; }
}
