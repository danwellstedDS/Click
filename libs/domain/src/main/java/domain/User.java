package domain;

import java.time.Instant;
import java.util.UUID;

public final class User {
  private final UUID id;
  private final String email;
  private final String passwordHash;
  private final String name;
  private final boolean isActive;
  private final Instant createdAt;
  private final Instant updatedAt;

  private User(UUID id, String email, String passwordHash, String name, boolean isActive, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.email = email;
    this.passwordHash = passwordHash;
    this.name = name;
    this.isActive = isActive;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static User create(UUID id, String email, String passwordHash, String name, boolean isActive, Instant createdAt, Instant updatedAt) {
    return new User(id, email, passwordHash, name, isActive, createdAt, updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getName() {
    return name;
  }

  public boolean isActive() {
    return isActive;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
