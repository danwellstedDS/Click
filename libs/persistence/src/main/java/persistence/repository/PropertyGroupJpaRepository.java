package persistence.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import persistence.entity.PropertyGroupEntity;

public interface PropertyGroupJpaRepository extends JpaRepository<PropertyGroupEntity, UUID> {
  Optional<PropertyGroupEntity> findByPrimaryOrgId(UUID primaryOrgId);
}
