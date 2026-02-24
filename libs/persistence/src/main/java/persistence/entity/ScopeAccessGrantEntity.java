package persistence.entity;

import domain.GrantRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "scope_access_grants")
public class ScopeAccessGrantEntity {
  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @Column(name = "scope_id", nullable = false)
  private UUID scopeId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private GrantRole role;

  @Column(name = "valid_from")
  private Instant validFrom;

  @Column(name = "valid_to")
  private Instant validTo;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected ScopeAccessGrantEntity() {
  }

  public ScopeAccessGrantEntity(UUID organizationId, UUID scopeId, GrantRole role) {
    this.organizationId = organizationId;
    this.scopeId = scopeId;
    this.role = role;
  }

  public UUID getId() {
    return id;
  }

  public UUID getOrganizationId() {
    return organizationId;
  }

  public UUID getScopeId() {
    return scopeId;
  }

  public GrantRole getRole() {
    return role;
  }

  public Instant getValidFrom() {
    return validFrom;
  }

  public Instant getValidTo() {
    return validTo;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
