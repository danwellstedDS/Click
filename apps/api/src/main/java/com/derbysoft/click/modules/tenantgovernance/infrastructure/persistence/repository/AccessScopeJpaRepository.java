package com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.repository;

import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.ScopeType;
import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.entity.AccessScopeEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessScopeJpaRepository extends JpaRepository<AccessScopeEntity, UUID> {
  List<AccessScopeEntity> findByPropertyGroupId(UUID propertyGroupId);
  Optional<AccessScopeEntity> findByPropertyGroupIdAndType(UUID propertyGroupId, ScopeType type);
  List<AccessScopeEntity> findByPropertyId(UUID propertyId);
}
