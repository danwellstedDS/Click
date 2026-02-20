package domain;

import java.time.Instant;
import java.util.UUID;

public final class User {
  private final UUID id;
  private final String email;
  private final String passwordHash;
  private final Instant createdAt;
  private final Instant updatedAt;

  private User(UUID id, String email, String passwordHash, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.email = email;
    this.passwordHash = passwordHash;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static User create(UUID id, String email, String passwordHash, Instant createdAt, Instant updatedAt) {
    return new User(id, email, passwordHash, createdAt, updatedAt);
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

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
