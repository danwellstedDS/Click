package com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mapped_facts")
public class MappedFactEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "mapping_run_id", nullable = false, updatable = false)
    private UUID mappingRunId;

    @Column(name = "canonical_fact_id", nullable = false, updatable = false)
    private UUID canonicalFactId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "resolved_org_node_id")
    private UUID resolvedOrgNodeId;

    @Column(name = "resolved_scope_type", length = 30)
    private String resolvedScopeType;

    @Column(name = "confidence_band", nullable = false, length = 20)
    private String confidenceBand;

    @Column(name = "confidence_score", nullable = false, precision = 4, scale = 3)
    private BigDecimal confidenceScore;

    @Column(name = "resolution_reason_code", nullable = false, length = 50)
    private String resolutionReasonCode;

    @Column(name = "rule_set_version", nullable = false, length = 20)
    private String ruleSetVersion;

    @Column(name = "override_applied", nullable = false)
    private boolean overrideApplied;

    @Column(name = "mapped_at", nullable = false, updatable = false)
    private Instant mappedAt;

    public MappedFactEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getMappingRunId() { return mappingRunId; }
    public void setMappingRunId(UUID mappingRunId) { this.mappingRunId = mappingRunId; }
    public UUID getCanonicalFactId() { return canonicalFactId; }
    public void setCanonicalFactId(UUID canonicalFactId) { this.canonicalFactId = canonicalFactId; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getResolvedOrgNodeId() { return resolvedOrgNodeId; }
    public void setResolvedOrgNodeId(UUID resolvedOrgNodeId) { this.resolvedOrgNodeId = resolvedOrgNodeId; }
    public String getResolvedScopeType() { return resolvedScopeType; }
    public void setResolvedScopeType(String resolvedScopeType) { this.resolvedScopeType = resolvedScopeType; }
    public String getConfidenceBand() { return confidenceBand; }
    public void setConfidenceBand(String confidenceBand) { this.confidenceBand = confidenceBand; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }
    public String getResolutionReasonCode() { return resolutionReasonCode; }
    public void setResolutionReasonCode(String resolutionReasonCode) { this.resolutionReasonCode = resolutionReasonCode; }
    public String getRuleSetVersion() { return ruleSetVersion; }
    public void setRuleSetVersion(String ruleSetVersion) { this.ruleSetVersion = ruleSetVersion; }
    public boolean isOverrideApplied() { return overrideApplied; }
    public void setOverrideApplied(boolean overrideApplied) { this.overrideApplied = overrideApplied; }
    public Instant getMappedAt() { return mappedAt; }
    public void setMappedAt(Instant mappedAt) { this.mappedAt = mappedAt; }
}
