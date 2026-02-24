package domain.repository;

import domain.Chain;
import java.util.Optional;
import java.util.UUID;

public interface ChainRepository {
  Optional<Chain> findById(UUID id);
  Optional<Chain> findByPrimaryOrgId(UUID primaryOrgId);
  Chain create(String name);
}
