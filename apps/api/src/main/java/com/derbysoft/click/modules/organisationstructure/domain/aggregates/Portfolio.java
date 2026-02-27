package com.derbysoft.click.modules.organisationstructure.domain.aggregates;

import java.time.Instant;
import java.util.UUID;

public final class Portfolio {
  private final UUID id;
  private final UUID propertyGroupId;
  private final String name;
  private final UUID ownerOrganizationId;
  private final Instant createdAt;
  private final Instant updatedAt;

  private Portfolio(UUID id, UUID propertyGroupId, String name, UUID ownerOrganizationId, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.propertyGroupId = propertyGroupId;
    this.name = name;
    this.ownerOrganizationId = ownerOrganizationId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Portfolio create(UUID id, UUID propertyGroupId, String name, UUID ownerOrganizationId, Instant createdAt, Instant updatedAt) {
    return new Portfolio(id, propertyGroupId, name, ownerOrganizationId, createdAt, updatedAt);
  }

  public UUID getId() { return id; }
  public UUID getPropertyGroupId() { return propertyGroupId; }
  public String getName() { return name; }
  public UUID getOwnerOrganizationId() { return ownerOrganizationId; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
