package com.derbysoft.click.modules.identityaccess.domain.aggregates;

import com.derbysoft.click.modules.identityaccess.domain.entities.TenantMembership;
import com.derbysoft.click.modules.identityaccess.domain.events.MembershipAdded;
import com.derbysoft.click.modules.identityaccess.domain.events.RoleAssigned;
import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class User {
  private final UUID id;
  private final String email;
  private final String passwordHash;
  private final String name;
  private final boolean isActive;
  private final Instant createdAt;
  private final Instant updatedAt;
  private final List<Object> events = new ArrayList<>();

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

  public TenantMembership addMembership(UUID membershipId, UUID tenantId, Role role) {
    Instant now = Instant.now();
    TenantMembership membership = new TenantMembership(membershipId, this.id, tenantId, role, now);
    events.add(new MembershipAdded(membershipId, this.id, tenantId, role, now));
    return membership;
  }

  public void assignRole(UUID membershipId, UUID tenantId, Role previousRole, Role newRole) {
    events.add(new RoleAssigned(membershipId, this.id, tenantId, previousRole, newRole, Instant.now()));
  }

  public List<Object> getEvents() {
    return Collections.unmodifiableList(events);
  }

  public void clearEvents() {
    events.clear();
  }

  public UUID getId() { return id; }
  public String getEmail() { return email; }
  public String getPasswordHash() { return passwordHash; }
  public String getName() { return name; }
  public boolean isActive() { return isActive; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
