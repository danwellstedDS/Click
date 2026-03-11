package com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mapping_overrides")
public class MappingOverrideEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "scope_type", nullable = false, length = 30, updatable = false)
    private String scopeType;

    @Column(name = "customer_account_id", nullable = false, length = 20, updatable = false)
    private String customerAccountId;

    @Column(name = "campaign_id", length = 30, updatable = false)
    private String campaignId;

    @Column(name = "target_org_node_id", nullable = false, updatable = false)
    private UUID targetOrgNodeId;

    @Column(name = "target_scope_type", nullable = false, length = 30, updatable = false)
    private String targetScopeType;

    @Column(nullable = false, columnDefinition = "TEXT", updatable = false)
    private String reason;

    @Column(nullable = false, length = 255, updatable = false)
    private String actor;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "removed_at")
    private Instant removedAt;

    @Column(name = "removed_reason", columnDefinition = "TEXT")
    private String removedReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public MappingOverrideEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public String getCustomerAccountId() { return customerAccountId; }
    public void setCustomerAccountId(String customerAccountId) { this.customerAccountId = customerAccountId; }
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    public UUID getTargetOrgNodeId() { return targetOrgNodeId; }
    public void setTargetOrgNodeId(UUID targetOrgNodeId) { this.targetOrgNodeId = targetOrgNodeId; }
    public String getTargetScopeType() { return targetScopeType; }
    public void setTargetScopeType(String targetScopeType) { this.targetScopeType = targetScopeType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getRemovedAt() { return removedAt; }
    public void setRemovedAt(Instant removedAt) { this.removedAt = removedAt; }
    public String getRemovedReason() { return removedReason; }
    public void setRemovedReason(String removedReason) { this.removedReason = removedReason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
