package com.derbysoft.click.modules.organisationstructure.domain.entities;

import com.derbysoft.click.modules.organisationstructure.domain.events.PropertyCreated;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class Property {
  private final UUID id;
  private final UUID propertyGroupId;
  private final String name;
  private final boolean isActive;
  private final String externalPropertyRef;
  private final Instant createdAt;
  private final Instant updatedAt;
  private final List<Object> events = new ArrayList<>();

  private Property(UUID id, UUID propertyGroupId, String name, boolean isActive, String externalPropertyRef, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.propertyGroupId = propertyGroupId;
    this.name = name;
    this.isActive = isActive;
    this.externalPropertyRef = externalPropertyRef;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /** Factory — creates a new Property and emits PropertyCreated. */
  public static Property create(UUID id, UUID propertyGroupId, String name, boolean isActive, String externalPropertyRef, Instant createdAt, Instant updatedAt) {
    Property property = new Property(id, propertyGroupId, name, isActive, externalPropertyRef, createdAt, updatedAt);
    property.events.add(new PropertyCreated(id, propertyGroupId, name, createdAt));
    return property;
  }

  /** Reconstitutes an existing property from persistence (no events emitted). */
  public static Property reconstitute(UUID id, UUID propertyGroupId, String name, boolean isActive, String externalPropertyRef, Instant createdAt, Instant updatedAt) {
    return new Property(id, propertyGroupId, name, isActive, externalPropertyRef, createdAt, updatedAt);
  }

  public List<Object> getEvents() {
    return Collections.unmodifiableList(events);
  }

  public void clearEvents() {
    events.clear();
  }

  public UUID getId() { return id; }
  public UUID getPropertyGroupId() { return propertyGroupId; }
  public String getName() { return name; }
  public boolean isActive() { return isActive; }
  public String getExternalPropertyRef() { return externalPropertyRef; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
