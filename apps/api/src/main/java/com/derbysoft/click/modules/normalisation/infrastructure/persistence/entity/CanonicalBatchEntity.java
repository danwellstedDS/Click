package com.derbysoft.click.modules.normalisation.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "canonical_batches")
public class CanonicalBatchEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "source_snapshot_id", nullable = false, updatable = false)
    private UUID sourceSnapshotId;

    @Column(name = "integration_id", nullable = false, updatable = false)
    private UUID integrationId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "account_id", nullable = false, length = 20, updatable = false)
    private String accountId;

    @Column(name = "mapping_version", nullable = false, length = 20, updatable = false)
    private String mappingVersion;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "fact_count", nullable = false)
    private int factCount;

    @Column(name = "quarantined_count", nullable = false)
    private int quarantinedCount;

    @Column(length = 64)
    private String checksum;

    @Column(name = "produced_at")
    private Instant producedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CanonicalBatchEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSourceSnapshotId() { return sourceSnapshotId; }
    public void setSourceSnapshotId(UUID sourceSnapshotId) { this.sourceSnapshotId = sourceSnapshotId; }
    public UUID getIntegrationId() { return integrationId; }
    public void setIntegrationId(UUID integrationId) { this.integrationId = integrationId; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getMappingVersion() { return mappingVersion; }
    public void setMappingVersion(String mappingVersion) { this.mappingVersion = mappingVersion; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getFactCount() { return factCount; }
    public void setFactCount(int factCount) { this.factCount = factCount; }
    public int getQuarantinedCount() { return quarantinedCount; }
    public void setQuarantinedCount(int quarantinedCount) { this.quarantinedCount = quarantinedCount; }
    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }
    public Instant getProducedAt() { return producedAt; }
    public void setProducedAt(Instant producedAt) { this.producedAt = producedAt; }
    public Instant getFailedAt() { return failedAt; }
    public void setFailedAt(Instant failedAt) { this.failedAt = failedAt; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
