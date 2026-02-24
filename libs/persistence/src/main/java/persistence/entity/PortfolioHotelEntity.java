package persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "portfolio_hotels")
public class PortfolioHotelEntity {
  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(name = "portfolio_id", nullable = false)
  private UUID portfolioId;

  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;

  protected PortfolioHotelEntity() {
  }

  public PortfolioHotelEntity(UUID portfolioId, UUID hotelId) {
    this.portfolioId = portfolioId;
    this.hotelId = hotelId;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPortfolioId() {
    return portfolioId;
  }

  public UUID getHotelId() {
    return hotelId;
  }
}
