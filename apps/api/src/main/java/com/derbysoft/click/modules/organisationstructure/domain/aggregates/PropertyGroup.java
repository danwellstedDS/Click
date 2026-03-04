package com.derbysoft.click.modules.organisationstructure.domain.aggregates;

import com.derbysoft.click.modules.organisationstructure.domain.events.ChainCreated;
import com.derbysoft.click.modules.organisationstructure.domain.events.ChainStatusChanged;
import com.derbysoft.click.modules.organisationstructure.domain.events.HierarchyChanged;
import com.derbysoft.click.modules.organisationstructure.domain.events.OrgNodeCreated;
import com.derbysoft.click.modules.organisationstructure.domain.valueobjects.ChainStatus;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class PropertyGroup {
  private final UUID id;
  private UUID parentId;
  private final String name;
  private final String timezone;
  private final String currency;
  private final UUID primaryOrgId;
  private final Instant createdAt;
  private Instant updatedAt;
  private ChainStatus status;
  private final List<Object> events = new ArrayList<>();

  private PropertyGroup(UUID id, UUID parentId, String name, String timezone, String currency, UUID primaryOrgId, Instant createdAt, Instant updatedAt, ChainStatus status) {
    this.id = id;
    this.parentId = parentId;
    this.name = name;
    this.timezone = timezone;
    this.currency = currency;
    this.primaryOrgId = primaryOrgId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.status = status;
  }

  /** Factory — creates a new PropertyGroup node and emits OrgNodeCreated and ChainCreated. */
  public static PropertyGroup create(UUID id, UUID parentId, String name, String timezone, String currency, UUID primaryOrgId, Instant createdAt, Instant updatedAt) {
    PropertyGroup pg = new PropertyGroup(id, parentId, name, timezone, currency, primaryOrgId, createdAt, updatedAt, ChainStatus.ACTIVE);
    pg.events.add(new OrgNodeCreated(id, name, parentId, createdAt));
    pg.events.add(new ChainCreated(id, name, createdAt));
    return pg;
  }

  /** Reconstitutes an existing aggregate from persistence (no events emitted). */
  public static PropertyGroup reconstitute(UUID id, UUID parentId, String name, String timezone, String currency, UUID primaryOrgId, Instant createdAt, Instant updatedAt, ChainStatus status) {
    return new PropertyGroup(id, parentId, name, timezone, currency, primaryOrgId, createdAt, updatedAt, status);
  }

  /** Moves this node to a new parent. Emits HierarchyChanged. */
  public void move(UUID newParentId) {
    UUID oldParentId = this.parentId;
    this.parentId = newParentId;
    this.updatedAt = Instant.now();
    events.add(new HierarchyChanged(this.id, oldParentId, newParentId, this.updatedAt));
  }

  /** Activates this chain. Throws Conflict if already ACTIVE. Emits ChainStatusChanged. */
  public void activate() {
    if (this.status == ChainStatus.ACTIVE) {
      throw new DomainError.Conflict("CHAIN_409", "Chain is already ACTIVE");
    }
    ChainStatus oldStatus = this.status;
    this.status = ChainStatus.ACTIVE;
    this.updatedAt = Instant.now();
    events.add(new ChainStatusChanged(this.id, oldStatus, ChainStatus.ACTIVE, this.updatedAt));
  }

  /** Deactivates this chain. Throws Conflict if already INACTIVE. Emits ChainStatusChanged. */
  public void deactivate() {
    if (this.status == ChainStatus.INACTIVE) {
      throw new DomainError.Conflict("CHAIN_409", "Chain is already INACTIVE");
    }
    ChainStatus oldStatus = this.status;
    this.status = ChainStatus.INACTIVE;
    this.updatedAt = Instant.now();
    events.add(new ChainStatusChanged(this.id, oldStatus, ChainStatus.INACTIVE, this.updatedAt));
  }

  public List<Object> getEvents() {
    return Collections.unmodifiableList(events);
  }

  public void clearEvents() {
    events.clear();
  }

  public UUID getId() { return id; }
  public UUID getParentId() { return parentId; }
  public String getName() { return name; }
  public String getTimezone() { return timezone; }
  public String getCurrency() { return currency; }
  public UUID getPrimaryOrgId() { return primaryOrgId; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public ChainStatus getStatus() { return status; }
}
