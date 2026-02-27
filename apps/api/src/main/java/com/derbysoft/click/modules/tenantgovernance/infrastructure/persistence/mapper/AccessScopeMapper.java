package com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.tenantgovernance.domain.entities.AccessScope;
import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.entity.AccessScopeEntity;

public final class AccessScopeMapper {
  private AccessScopeMapper() {}

  public static AccessScope toDomain(AccessScopeEntity entity) {
    return new AccessScope(
        entity.getId(),
        entity.getType(),
        entity.getPropertyGroupId(),
        entity.getPropertyId(),
        entity.getPortfolioId(),
        entity.getCreatedAt()
    );
  }
}
