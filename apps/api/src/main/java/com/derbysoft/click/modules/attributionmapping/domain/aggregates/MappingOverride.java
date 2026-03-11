package com.derbysoft.click.modules.attributionmapping.domain.aggregates;

import com.derbysoft.click.modules.attributionmapping.domain.events.MappingOverrideRemoved;
import com.derbysoft.click.modules.attributionmapping.domain.events.MappingOverrideSet;
import com.derbysoft.click.modules.attributionmapping.domain.valueobjects.OverrideScope;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class MappingOverride {

    private final UUID id;
    private final UUID tenantId;
    private final OverrideScope scopeType;
    private final String customerAccountId;
    private final String campaignId;
    private final UUID targetOrgNodeId;
    private final String targetScopeType;
    private final String reason;
    private final String actor;
    private String status;
    private Instant removedAt;
    private String removedReason;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<Object> events = new ArrayList<>();

    private MappingOverride(
        UUID id, UUID tenantId, OverrideScope scopeType,
        String customerAccountId, String campaignId,
        UUID targetOrgNodeId, String targetScopeType,
        String reason, String actor, String status,
        Instant removedAt, String removedReason,
        Instant createdAt, Instant updatedAt
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.scopeType = scopeType;
        this.customerAccountId = customerAccountId;
        this.campaignId = campaignId;
        this.targetOrgNodeId = targetOrgNodeId;
        this.targetScopeType = targetScopeType;
        this.reason = reason;
        this.actor = actor;
        this.status = status;
        this.removedAt = removedAt;
        this.removedReason = removedReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MappingOverride set(
        UUID tenantId, OverrideScope scopeType,
        String customerAccountId, String campaignId,
        UUID targetOrgNodeId, String targetScopeType,
        String reason, String actor, Instant now
    ) {
        UUID id = UUID.randomUUID();
        MappingOverride override = new MappingOverride(
            id, tenantId, scopeType, customerAccountId, campaignId,
            targetOrgNodeId, targetScopeType, reason, actor,
            "ACTIVE", null, null, now, now
        );
        override.events.add(new MappingOverrideSet(
            id, tenantId, scopeType.name(), customerAccountId, campaignId, targetOrgNodeId, actor, now
        ));
        return override;
    }

    public static MappingOverride reconstitute(
        UUID id, UUID tenantId, OverrideScope scopeType,
        String customerAccountId, String campaignId,
        UUID targetOrgNodeId, String targetScopeType,
        String reason, String actor, String status,
        Instant removedAt, String removedReason,
        Instant createdAt, Instant updatedAt
    ) {
        return new MappingOverride(
            id, tenantId, scopeType, customerAccountId, campaignId,
            targetOrgNodeId, targetScopeType, reason, actor, status,
            removedAt, removedReason, createdAt, updatedAt
        );
    }

    public void remove(String reason, String actor, Instant now) {
        if ("REMOVED".equals(this.status)) {
            throw new IllegalStateException("Override " + id + " is already REMOVED");
        }
        this.status = "REMOVED";
        this.removedAt = now;
        this.removedReason = reason;
        this.updatedAt = now;
        events.add(new MappingOverrideRemoved(id, tenantId, actor, reason, now));
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public OverrideScope getScopeType() { return scopeType; }
    public String getCustomerAccountId() { return customerAccountId; }
    public String getCampaignId() { return campaignId; }
    public UUID getTargetOrgNodeId() { return targetOrgNodeId; }
    public String getTargetScopeType() { return targetScopeType; }
    public String getReason() { return reason; }
    public String getActor() { return actor; }
    public String getStatus() { return status; }
    public Instant getRemovedAt() { return removedAt; }
    public String getRemovedReason() { return removedReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<Object> getEvents() { return Collections.unmodifiableList(events); }
    public void clearEvents() { events.clear(); }
}
