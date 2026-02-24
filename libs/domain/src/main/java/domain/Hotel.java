package domain;

import java.time.Instant;
import java.util.UUID;

public final class Hotel {
  private final UUID id;
  private final UUID chainId;
  private final String name;
  private final boolean isActive;
  private final String externalHotelRef;
  private final Instant createdAt;
  private final Instant updatedAt;

  private Hotel(UUID id, UUID chainId, String name, boolean isActive, String externalHotelRef, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.chainId = chainId;
    this.name = name;
    this.isActive = isActive;
    this.externalHotelRef = externalHotelRef;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Hotel create(UUID id, UUID chainId, String name, boolean isActive, String externalHotelRef, Instant createdAt, Instant updatedAt) {
    return new Hotel(id, chainId, name, isActive, externalHotelRef, createdAt, updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public UUID getChainId() {
    return chainId;
  }

  public String getName() {
    return name;
  }

  public boolean isActive() {
    return isActive;
  }

  public String getExternalHotelRef() {
    return externalHotelRef;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
