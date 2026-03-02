package com.derbysoft.click.modules.identityaccess.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "tenant_memberships")
public class TenantMembershipEntity implements Persistable<UUID> {
  @Id
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

  @Transient
  private boolean newEntity;

  protected TenantMembershipEntity() {}

  public TenantMembershipEntity(UUID id, UUID userId, UUID tenantId, String role) {
    this.id = id;
    this.userId = userId;
    this.tenantId = tenantId;
    this.role = role;
    this.newEntity = true;
  }

  @Override
  public UUID getId() { return id; }

  @Override
  public boolean isNew() { return newEntity; }
  public UUID getUserId() { return userId; }
  public UUID getTenantId() { return tenantId; }
  public String getRole() { return role; }
  public Instant getCreatedAt() { return createdAt; }
}
