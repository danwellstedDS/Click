package persistence.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import persistence.entity.PortfolioHotelEntity;

public interface PortfolioHotelJpaRepository extends JpaRepository<PortfolioHotelEntity, UUID> {
}
