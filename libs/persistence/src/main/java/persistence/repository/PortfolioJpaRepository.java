package persistence.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import persistence.entity.PortfolioEntity;

public interface PortfolioJpaRepository extends JpaRepository<PortfolioEntity, UUID> {
  List<PortfolioEntity> findAllByPropertyGroupId(UUID propertyGroupId);
}
