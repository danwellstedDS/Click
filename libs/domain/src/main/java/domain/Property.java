package domain;

import java.time.Instant;
import java.util.UUID;

public final class Property {
  private final UUID id;
  private final UUID propertyGroupId;
  private final String name;
  private final boolean isActive;
  private final String externalPropertyRef;
  private final Instant createdAt;
  private final Instant updatedAt;

  private Property(UUID id, UUID propertyGroupId, String name, boolean isActive, String externalPropertyRef, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.propertyGroupId = propertyGroupId;
    this.name = name;
    this.isActive = isActive;
    this.externalPropertyRef = externalPropertyRef;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Property create(UUID id, UUID propertyGroupId, String name, boolean isActive, String externalPropertyRef, Instant createdAt, Instant updatedAt) {
    return new Property(id, propertyGroupId, name, isActive, externalPropertyRef, createdAt, updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public UUID getPropertyGroupId() {
    return propertyGroupId;
  }

  public String getName() {
    return name;
  }

  public boolean isActive() {
    return isActive;
  }

  public String getExternalPropertyRef() {
    return externalPropertyRef;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
