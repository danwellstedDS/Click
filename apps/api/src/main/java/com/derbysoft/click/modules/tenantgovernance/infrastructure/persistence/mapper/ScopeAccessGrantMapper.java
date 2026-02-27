package com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.tenantgovernance.domain.entities.ScopeAccessGrant;
import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.entity.ScopeAccessGrantEntity;

public final class ScopeAccessGrantMapper {
  private ScopeAccessGrantMapper() {}

  public static ScopeAccessGrant toDomain(ScopeAccessGrantEntity entity) {
    return new ScopeAccessGrant(
        entity.getId(),
        entity.getOrganizationId(),
        entity.getScopeId(),
        entity.getRole(),
        entity.getValidFrom(),
        entity.getValidTo(),
        entity.getCreatedAt()
    );
  }
}
