package persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "portfolio_properties")
public class PortfolioPropertyEntity {
  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(name = "portfolio_id", nullable = false)
  private UUID portfolioId;

  @Column(name = "property_id", nullable = false)
  private UUID propertyId;

  protected PortfolioPropertyEntity() {
  }

  public PortfolioPropertyEntity(UUID portfolioId, UUID propertyId) {
    this.portfolioId = portfolioId;
    this.propertyId = propertyId;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPortfolioId() {
    return portfolioId;
  }

  public UUID getPropertyId() {
    return propertyId;
  }
}
