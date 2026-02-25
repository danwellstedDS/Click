package domain.repository;

import domain.Portfolio;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioRepository {
  Optional<Portfolio> findById(UUID id);
  List<Portfolio> findAllByPropertyGroupId(UUID propertyGroupId);
  Portfolio create(UUID propertyGroupId, String name);
}
