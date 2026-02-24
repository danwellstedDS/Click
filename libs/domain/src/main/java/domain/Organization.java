package domain;

import java.time.Instant;
import java.util.UUID;

public final class Organization {
  private final UUID id;
  private final String name;
  private final OrganizationType type;
  private final Instant createdAt;
  private final Instant updatedAt;

  private Organization(UUID id, String name, OrganizationType type, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Organization create(UUID id, String name, OrganizationType type, Instant createdAt, Instant updatedAt) {
    return new Organization(id, name, type, createdAt, updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public OrganizationType getType() {
    return type;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
