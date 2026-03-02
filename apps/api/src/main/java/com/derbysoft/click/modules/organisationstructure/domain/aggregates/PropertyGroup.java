package com.derbysoft.click.modules.organisationstructure.domain.aggregates;

import com.derbysoft.click.modules.organisationstructure.domain.events.HierarchyChanged;
import com.derbysoft.click.modules.organisationstructure.domain.events.OrgNodeCreated;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class PropertyGroup {
  private final UUID id;
  private UUID parentId;
  private final String name;
  private final String timezone;
  private final String currency;
  private final UUID primaryOrgId;
  private final Instant createdAt;
  private Instant updatedAt;
  private final List<Object> events = new ArrayList<>();

  private PropertyGroup(UUID id, UUID parentId, String name, String timezone, String currency, UUID primaryOrgId, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.parentId = parentId;
    this.name = name;
    this.timezone = timezone;
    this.currency = currency;
    this.primaryOrgId = primaryOrgId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /** Factory — creates a new PropertyGroup node and emits OrgNodeCreated. */
  public static PropertyGroup create(UUID id, UUID parentId, String name, String timezone, String currency, UUID primaryOrgId, Instant createdAt, Instant updatedAt) {
    PropertyGroup pg = new PropertyGroup(id, parentId, name, timezone, currency, primaryOrgId, createdAt, updatedAt);
    pg.events.add(new OrgNodeCreated(id, name, parentId, createdAt));
    return pg;
  }

  /** Reconstitutes an existing aggregate from persistence (no events emitted). */
  public static PropertyGroup reconstitute(UUID id, UUID parentId, String name, String timezone, String currency, UUID primaryOrgId, Instant createdAt, Instant updatedAt) {
    return new PropertyGroup(id, parentId, name, timezone, currency, primaryOrgId, createdAt, updatedAt);
  }

  /** Moves this node to a new parent. Emits HierarchyChanged. */
  public void move(UUID newParentId) {
    UUID oldParentId = this.parentId;
    this.parentId = newParentId;
    this.updatedAt = Instant.now();
    events.add(new HierarchyChanged(this.id, oldParentId, newParentId, this.updatedAt));
  }

  public List<Object> getEvents() {
    return Collections.unmodifiableList(events);
  }

  public void clearEvents() {
    events.clear();
  }

  public UUID getId() { return id; }
  public UUID getParentId() { return parentId; }
  public String getName() { return name; }
  public String getTimezone() { return timezone; }
  public String getCurrency() { return currency; }
  public UUID getPrimaryOrgId() { return primaryOrgId; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
