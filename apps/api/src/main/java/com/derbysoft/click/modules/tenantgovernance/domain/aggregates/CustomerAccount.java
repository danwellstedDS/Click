package com.derbysoft.click.modules.tenantgovernance.domain.aggregates;

import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.CustomerType;
import java.time.Instant;
import java.util.UUID;

public final class CustomerAccount {
  private final UUID id;
  private final CustomerType type;
  private final String name;
  private final UUID organizationId;
  private final Instant createdAt;
  private final Instant updatedAt;

  private CustomerAccount(UUID id, CustomerType type, String name, UUID organizationId, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.type = type;
    this.name = name;
    this.organizationId = organizationId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static CustomerAccount create(UUID id, CustomerType type, String name, UUID organizationId, Instant createdAt, Instant updatedAt) {
    return new CustomerAccount(id, type, name, organizationId, createdAt, updatedAt);
  }

  public UUID getId() { return id; }
  public CustomerType getType() { return type; }
  public String getName() { return name; }
  public UUID getOrganizationId() { return organizationId; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
