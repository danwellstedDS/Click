package persistence.entity;

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
@Table(name = "org_memberships")
public class OrgMembershipEntity {
  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @Column(name = "is_org_admin", nullable = false)
  private boolean isOrgAdmin = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected OrgMembershipEntity() {
  }

  public OrgMembershipEntity(UUID userId, UUID organizationId, boolean isOrgAdmin) {
    this.userId = userId;
    this.organizationId = organizationId;
    this.isOrgAdmin = isOrgAdmin;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getOrganizationId() {
    return organizationId;
  }

  public boolean isOrgAdmin() {
    return isOrgAdmin;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
