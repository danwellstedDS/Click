package persistence.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import persistence.entity.ChainEntity;

public interface ChainJpaRepository extends JpaRepository<ChainEntity, UUID> {
  java.util.Optional<ChainEntity> findByPrimaryOrgId(UUID primaryOrgId);
}
