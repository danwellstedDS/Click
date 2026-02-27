package com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.tenantgovernance.domain.aggregates.Organization;
import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.entity.OrganizationEntity;

public final class OrganizationMapper {
  private OrganizationMapper() {}

  public static Organization toDomain(OrganizationEntity entity) {
    return Organization.create(
        entity.getId(),
        entity.getName(),
        entity.getType(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }
}
