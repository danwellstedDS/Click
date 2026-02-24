package persistence.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import persistence.entity.ScopeAccessGrantEntity;

public interface ScopeAccessGrantJpaRepository extends JpaRepository<ScopeAccessGrantEntity, UUID> {
  List<ScopeAccessGrantEntity> findByOrganizationId(UUID organizationId);
  List<ScopeAccessGrantEntity> findByScopeId(UUID scopeId);
}
