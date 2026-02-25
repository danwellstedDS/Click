package persistence.entity;

import domain.ScopeType;
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
@Table(name = "access_scopes")
public class AccessScopeEntity {
  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ScopeType type;

  @Column(name = "property_group_id", nullable = false)
  private UUID propertyGroupId;

  @Column(name = "property_id")
  private UUID propertyId;

  @Column(name = "portfolio_id")
  private UUID portfolioId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected AccessScopeEntity() {
  }

  public AccessScopeEntity(ScopeType type, UUID propertyGroupId, UUID propertyId, UUID portfolioId) {
    this.type = type;
    this.propertyGroupId = propertyGroupId;
    this.propertyId = propertyId;
    this.portfolioId = portfolioId;
  }

  public UUID getId() {
    return id;
  }

  public ScopeType getType() {
    return type;
  }

  public UUID getPropertyGroupId() {
    return propertyGroupId;
  }

  public UUID getPropertyId() {
    return propertyId;
  }

  public UUID getPortfolioId() {
    return portfolioId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
