package domain;

import java.time.Instant;
import java.util.UUID;

public final class Tenant {
  private final UUID id;
  private final String name;
  private final Instant createdAt;
  private final Instant updatedAt;

  private Tenant(UUID id, String name, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.name = name;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Tenant create(UUID id, String name, Instant createdAt, Instant updatedAt) {
    return new Tenant(id, name, createdAt, updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
