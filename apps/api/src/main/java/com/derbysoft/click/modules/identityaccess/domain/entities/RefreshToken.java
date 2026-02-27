package com.derbysoft.click.modules.identityaccess.domain.entities;

import java.time.Instant;
import java.util.UUID;

public final class RefreshToken {
  private final UUID id;
  private final UUID userId;
  private final String tokenHash;
  private final Instant expiresAt;
  private final Instant createdAt;

  private RefreshToken(UUID id, UUID userId, String tokenHash, Instant expiresAt, Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
  }

  public static RefreshToken create(UUID id, UUID userId, String tokenHash, Instant expiresAt, Instant createdAt) {
    return new RefreshToken(id, userId, tokenHash, expiresAt, createdAt);
  }

  public UUID getId() { return id; }
  public UUID getUserId() { return userId; }
  public String getTokenHash() { return tokenHash; }
  public Instant getExpiresAt() { return expiresAt; }
  public Instant getCreatedAt() { return createdAt; }
}
