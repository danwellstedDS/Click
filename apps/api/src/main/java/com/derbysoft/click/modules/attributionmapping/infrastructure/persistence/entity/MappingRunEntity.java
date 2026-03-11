package com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mapping_runs")
public class MappingRunEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "canonical_batch_id", nullable = false, updatable = false)
    private UUID canonicalBatchId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "rule_set_version", nullable = false, length = 20)
    private String ruleSetVersion;

    @Column(name = "override_set_version", nullable = false, length = 64)
    private String overrideSetVersion;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "mapped_count", nullable = false)
    private int mappedCount;

    @Column(name = "low_confidence_count", nullable = false)
    private int lowConfidenceCount;

    @Column(name = "unresolved_count", nullable = false)
    private int unresolvedCount;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public MappingRunEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCanonicalBatchId() { return canonicalBatchId; }
    public void setCanonicalBatchId(UUID canonicalBatchId) { this.canonicalBatchId = canonicalBatchId; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getRuleSetVersion() { return ruleSetVersion; }
    public void setRuleSetVersion(String ruleSetVersion) { this.ruleSetVersion = ruleSetVersion; }
    public String getOverrideSetVersion() { return overrideSetVersion; }
    public void setOverrideSetVersion(String overrideSetVersion) { this.overrideSetVersion = overrideSetVersion; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getMappedCount() { return mappedCount; }
    public void setMappedCount(int mappedCount) { this.mappedCount = mappedCount; }
    public int getLowConfidenceCount() { return lowConfidenceCount; }
    public void setLowConfidenceCount(int lowConfidenceCount) { this.lowConfidenceCount = lowConfidenceCount; }
    public int getUnresolvedCount() { return unresolvedCount; }
    public void setUnresolvedCount(int unresolvedCount) { this.unresolvedCount = unresolvedCount; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getFailedAt() { return failedAt; }
    public void setFailedAt(Instant failedAt) { this.failedAt = failedAt; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
