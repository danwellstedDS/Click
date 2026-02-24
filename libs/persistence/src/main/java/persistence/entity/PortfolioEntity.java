package persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "portfolios")
public class PortfolioEntity {
  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(name = "chain_id", nullable = false)
  private UUID chainId;

  @Column(nullable = false)
  private String name;

  @Column(name = "owner_organization_id")
  private UUID ownerOrganizationId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected PortfolioEntity() {
  }

  public PortfolioEntity(UUID chainId, String name) {
    this.chainId = chainId;
    this.name = name;
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

  public UUID getOwnerOrganizationId() {
    return ownerOrganizationId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
