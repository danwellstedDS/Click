package domain.repository;

import domain.Portfolio;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioRepository {
  Optional<Portfolio> findById(UUID id);
  List<Portfolio> findAllByChainId(UUID chainId);
  Portfolio create(UUID chainId, String name);
}
