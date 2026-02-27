package com.derbysoft.click.modules.tenantgovernance.domain;

import com.derbysoft.click.modules.tenantgovernance.domain.entities.AccessScope;
import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.ScopeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccessScopeRepository {
  List<AccessScope> findByPropertyGroupId(UUID propertyGroupId);
  Optional<AccessScope> findByPropertyGroupIdAndType(UUID propertyGroupId, ScopeType type);
  List<AccessScope> findByPropertyId(UUID propertyId);
  AccessScope create(ScopeType type, UUID propertyGroupId, UUID propertyId, UUID portfolioId);
}
