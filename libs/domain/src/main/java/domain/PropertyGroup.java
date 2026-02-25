package domain;

import java.time.Instant;
import java.util.UUID;

public final class PropertyGroup {
  private final UUID id;
  private final UUID parentId;
  private final String name;
  private final String timezone;
  private final String currency;
  private final UUID primaryOrgId;
  private final Instant createdAt;
  private final Instant updatedAt;

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

  public static PropertyGroup create(UUID id, UUID parentId, String name, String timezone, String currency, UUID primaryOrgId, Instant createdAt, Instant updatedAt) {
    return new PropertyGroup(id, parentId, name, timezone, currency, primaryOrgId, createdAt, updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public UUID getParentId() {
    return parentId;
  }

  public String getName() {
    return name;
  }

  public String getTimezone() {
    return timezone;
  }

  public String getCurrency() {
    return currency;
  }

  public UUID getPrimaryOrgId() {
    return primaryOrgId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
