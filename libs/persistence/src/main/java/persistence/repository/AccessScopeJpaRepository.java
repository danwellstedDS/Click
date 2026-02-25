package persistence.repository;

import domain.ScopeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import persistence.entity.AccessScopeEntity;

public interface AccessScopeJpaRepository extends JpaRepository<AccessScopeEntity, UUID> {
  List<AccessScopeEntity> findByPropertyGroupId(UUID propertyGroupId);
  Optional<AccessScopeEntity> findByPropertyGroupIdAndType(UUID propertyGroupId, ScopeType type);
  List<AccessScopeEntity> findByPropertyId(UUID propertyId);
}
