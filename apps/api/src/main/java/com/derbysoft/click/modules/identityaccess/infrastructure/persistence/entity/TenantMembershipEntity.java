package com.derbysoft.click.modules.identityaccess.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "tenant_memberships")
public class TenantMembershipEntity {
  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "role", nullable = false)
  private String role;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected TenantMembershipEntity() {}

  public TenantMembershipEntity(UUID id, UUID userId, UUID tenantId, String role) {
    this.id = id;
    this.userId = userId;
    this.tenantId = tenantId;
    this.role = role;
  }

  public UUID getId() { return id; }
  public UUID getUserId() { return userId; }
  public UUID getTenantId() { return tenantId; }
  public String getRole() { return role; }
  public Instant getCreatedAt() { return createdAt; }
}
