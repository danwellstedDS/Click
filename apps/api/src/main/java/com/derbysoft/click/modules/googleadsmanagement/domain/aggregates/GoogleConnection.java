package com.derbysoft.click.modules.googleadsmanagement.domain.aggregates;

import com.derbysoft.click.modules.googleadsmanagement.domain.events.ConnectionBroken;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.ConnectionCreated;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.ConnectionCredentialRotated;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.ConnectionRecovered;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.ConnectionStatus;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class GoogleConnection {

    private final UUID id;
    private final UUID tenantId;
    private final String managerId;
    private ConnectionStatus status;
    private String credentialPath;
    private Instant lastDiscoveredAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<Object> events = new ArrayList<>();

    private GoogleConnection(
        UUID id,
        UUID tenantId,
        String managerId,
        ConnectionStatus status,
        String credentialPath,
        Instant lastDiscoveredAt,
        Instant createdAt,
        Instant updatedAt
    ) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(managerId, "managerId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(credentialPath, "credentialPath must not be null");
        this.id = id;
        this.tenantId = tenantId;
        this.managerId = managerId;
        this.status = status;
        this.credentialPath = credentialPath;
        this.lastDiscoveredAt = lastDiscoveredAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static GoogleConnection reconstitute(
        UUID id,
        UUID tenantId,
        String managerId,
        ConnectionStatus status,
        String credentialPath,
        Instant lastDiscoveredAt,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new GoogleConnection(id, tenantId, managerId, status, credentialPath,
            lastDiscoveredAt, createdAt, updatedAt);
    }

    public static GoogleConnection create(UUID id, UUID tenantId, String managerId, String credentialPath, Instant now) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(managerId, "managerId must not be null");
        Objects.requireNonNull(credentialPath, "credentialPath must not be null");
        GoogleConnection conn = new GoogleConnection(id, tenantId, managerId,
            ConnectionStatus.ACTIVE, credentialPath, null, now, now);
        conn.events.add(new ConnectionCreated(id, tenantId, managerId, now));
        return conn;
    }

    public void rotateCredential(String newCredentialPath) {
        Objects.requireNonNull(newCredentialPath, "newCredentialPath must not be null");
        this.credentialPath = newCredentialPath;
        this.updatedAt = Instant.now();
        events.add(new ConnectionCredentialRotated(id, updatedAt));
    }

    public void markBroken(String reason) {
        if (status != ConnectionStatus.ACTIVE) {
            throw new DomainError.Conflict("GCONN_001",
                "Cannot markBroken: connection is not ACTIVE; current status: " + status);
        }
        this.status = ConnectionStatus.BROKEN;
        this.updatedAt = Instant.now();
        events.add(new ConnectionBroken(id, reason, updatedAt));
    }

    public void recover() {
        if (status != ConnectionStatus.BROKEN) {
            throw new DomainError.Conflict("GCONN_002",
                "Cannot recover: connection is not BROKEN; current status: " + status);
        }
        this.status = ConnectionStatus.ACTIVE;
        this.updatedAt = Instant.now();
        events.add(new ConnectionRecovered(id, updatedAt));
    }

    public void recordDiscovery(Instant now) {
        this.lastDiscoveredAt = now;
        this.updatedAt = now;
    }

    public List<Object> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void clearEvents() {
        events.clear();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getManagerId() { return managerId; }
    public ConnectionStatus getStatus() { return status; }
    public String getCredentialPath() { return credentialPath; }
    public Instant getLastDiscoveredAt() { return lastDiscoveredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
