package com.derbysoft.click.modules.campaignexecution.domain.aggregates;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class CampaignPlan {

    private final UUID id;
    private final UUID tenantId;
    private String name;
    private String description;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<Object> events = new ArrayList<>();

    private CampaignPlan(UUID id, UUID tenantId, String name, String description,
                          Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CampaignPlan create(UUID id, UUID tenantId, String name, String description,
                                       Instant now) {
        return new CampaignPlan(id, tenantId, name, description, now, now);
    }

    public static CampaignPlan reconstitute(UUID id, UUID tenantId, String name, String description,
                                             Instant createdAt, Instant updatedAt) {
        return new CampaignPlan(id, tenantId, name, description, createdAt, updatedAt);
    }

    public void rename(String name, Instant now) {
        this.name = name;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<Object> getEvents() { return Collections.unmodifiableList(events); }
    public void clearEvents() { events.clear(); }
}
