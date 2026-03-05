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
@Table(name = "google_connections")
public class GoogleConnectionEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, unique = true, updatable = false)
    private UUID tenantId;

    @Column(name = "manager_id", nullable = false, length = 20, updatable = false)
    private String managerId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "credential_path", nullable = false, length = 500)
    private String credentialPath;

    @Column(name = "last_discovered_at")
    private Instant lastDiscoveredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected GoogleConnectionEntity() {}

    public GoogleConnectionEntity(
        UUID id,
        UUID tenantId,
        String managerId,
        String status,
        String credentialPath,
        Instant lastDiscoveredAt
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.managerId = managerId;
        this.status = status;
        this.credentialPath = credentialPath;
        this.lastDiscoveredAt = lastDiscoveredAt;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getManagerId() { return managerId; }
    public String getStatus() { return status; }
    public String getCredentialPath() { return credentialPath; }
    public Instant getLastDiscoveredAt() { return lastDiscoveredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(String status) { this.status = status; }
    public void setCredentialPath(String credentialPath) { this.credentialPath = credentialPath; }
    public void setLastDiscoveredAt(Instant lastDiscoveredAt) { this.lastDiscoveredAt = lastDiscoveredAt; }
}
