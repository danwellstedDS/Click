package com.derbysoft.click.modules.googleadsmanagement.domain.aggregates;

import com.derbysoft.click.modules.googleadsmanagement.domain.events.BindingBroken;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.BindingCreated;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.BindingRecovered;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.BindingRemoved;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.BindingStaleFlagged;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.BindingStaleRecovered;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.BindingStatus;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.BindingType;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class AccountBinding {

    private final UUID id;
    private final UUID connectionId;
    private final UUID tenantId;
    private final String customerId;
    private BindingStatus status;
    private final BindingType bindingType;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<Object> events = new ArrayList<>();

    private AccountBinding(
        UUID id,
        UUID connectionId,
        UUID tenantId,
        String customerId,
        BindingStatus status,
        BindingType bindingType,
        Instant createdAt,
        Instant updatedAt
    ) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(connectionId, "connectionId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(bindingType, "bindingType must not be null");
        this.id = id;
        this.connectionId = connectionId;
        this.tenantId = tenantId;
        this.customerId = customerId;
        this.status = status;
        this.bindingType = bindingType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AccountBinding reconstitute(
        UUID id,
        UUID connectionId,
        UUID tenantId,
        String customerId,
        BindingStatus status,
        BindingType bindingType,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new AccountBinding(id, connectionId, tenantId, customerId,
            status, bindingType, createdAt, updatedAt);
    }

    public static AccountBinding create(
        UUID id, UUID connectionId, UUID tenantId, String customerId, BindingType bindingType, Instant now
    ) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(connectionId, "connectionId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(bindingType, "bindingType must not be null");
        AccountBinding binding = new AccountBinding(id, connectionId, tenantId, customerId,
            BindingStatus.ACTIVE, bindingType, now, now);
        binding.events.add(new BindingCreated(id, connectionId, tenantId, customerId, now));
        return binding;
    }

    public void remove() {
        if (status == BindingStatus.REMOVED) {
            throw new DomainError.Conflict("BIND_001",
                "Cannot remove: binding is already REMOVED");
        }
        this.status = BindingStatus.REMOVED;
        this.updatedAt = Instant.now();
        events.add(new BindingRemoved(id, connectionId, tenantId, customerId, updatedAt));
    }

    public void markBroken(String reason) {
        if (status != BindingStatus.ACTIVE) {
            throw new DomainError.Conflict("BIND_002",
                "Cannot markBroken: binding is not ACTIVE; current status: " + status);
        }
        this.status = BindingStatus.BROKEN;
        this.updatedAt = Instant.now();
        events.add(new BindingBroken(id, reason, updatedAt));
    }

    public void recover() {
        if (status != BindingStatus.BROKEN) {
            throw new DomainError.Conflict("BIND_003",
                "Cannot recover: binding is not BROKEN; current status: " + status);
        }
        this.status = BindingStatus.ACTIVE;
        this.updatedAt = Instant.now();
        events.add(new BindingRecovered(id, updatedAt));
    }

    public void flagStale() {
        if (status != BindingStatus.ACTIVE) {
            throw new DomainError.Conflict("BIND_004",
                "Cannot flagStale: binding is not ACTIVE; current status: " + status);
        }
        this.status = BindingStatus.STALE;
        this.updatedAt = Instant.now();
        events.add(new BindingStaleFlagged(id, updatedAt));
    }

    public void recoverFromStale() {
        if (status != BindingStatus.STALE) {
            throw new DomainError.Conflict("BIND_005",
                "Cannot recoverFromStale: binding is not STALE; current status: " + status);
        }
        this.status = BindingStatus.ACTIVE;
        this.updatedAt = Instant.now();
        events.add(new BindingStaleRecovered(id, updatedAt));
    }

    public List<Object> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void clearEvents() {
        events.clear();
    }

    public UUID getId() { return id; }
    public UUID getConnectionId() { return connectionId; }
    public UUID getTenantId() { return tenantId; }
    public String getCustomerId() { return customerId; }
    public BindingStatus getStatus() { return status; }
    public BindingType getBindingType() { return bindingType; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
