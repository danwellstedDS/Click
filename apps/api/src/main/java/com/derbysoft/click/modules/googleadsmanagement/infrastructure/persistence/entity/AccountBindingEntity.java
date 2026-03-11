package com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "account_bindings")
public class AccountBindingEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "connection_id", nullable = false, updatable = false)
    private UUID connectionId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "customer_id", nullable = false, length = 20, updatable = false)
    private String customerId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "binding_type", nullable = false, length = 20, updatable = false)
    private String bindingType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AccountBindingEntity() {}

    public AccountBindingEntity(
        UUID id,
        UUID connectionId,
        UUID tenantId,
        String customerId,
        String status,
        String bindingType
    ) {
        this.id = id;
        this.connectionId = connectionId;
        this.tenantId = tenantId;
        this.customerId = customerId;
        this.status = status;
        this.bindingType = bindingType;
    }

    public UUID getId() { return id; }
    public UUID getConnectionId() { return connectionId; }
    public UUID getTenantId() { return tenantId; }
    public String getCustomerId() { return customerId; }
    public String getStatus() { return status; }
    public String getBindingType() { return bindingType; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Column(name = "org_node_id")
    private UUID orgNodeId;

    @Column(name = "org_scope_type", length = 30)
    private String orgScopeType;

    public void setStatus(String status) { this.status = status; }
    public UUID getOrgNodeId() { return orgNodeId; }
    public void setOrgNodeId(UUID orgNodeId) { this.orgNodeId = orgNodeId; }
    public String getOrgScopeType() { return orgScopeType; }
    public void setOrgScopeType(String orgScopeType) { this.orgScopeType = orgScopeType; }
}
