package domain;

import java.time.Instant;
import java.util.UUID;

public final class Chain {
  private final UUID id;
  private final String name;
  private final String timezone;
  private final String currency;
  private final UUID primaryOrgId;
  private final Instant createdAt;
  private final Instant updatedAt;

  private Chain(UUID id, String name, String timezone, String currency, UUID primaryOrgId, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.name = name;
    this.timezone = timezone;
    this.currency = currency;
    this.primaryOrgId = primaryOrgId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Chain create(UUID id, String name, String timezone, String currency, UUID primaryOrgId, Instant createdAt, Instant updatedAt) {
    return new Chain(id, name, timezone, currency, primaryOrgId, createdAt, updatedAt);
  }

  public UUID getId() {
    return id;
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
