package com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.repository;

import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.entity.ScopeAccessGrantEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScopeAccessGrantJpaRepository extends JpaRepository<ScopeAccessGrantEntity, UUID> {
  List<ScopeAccessGrantEntity> findByOrganizationId(UUID organizationId);
  List<ScopeAccessGrantEntity> findByScopeId(UUID scopeId);
}
