package com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.organisationstructure.domain.aggregates.Portfolio;
import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.entity.PortfolioEntity;

public final class PortfolioMapper {
  private PortfolioMapper() {}

  public static Portfolio toDomain(PortfolioEntity entity) {
    return Portfolio.create(
        entity.getId(),
        entity.getPropertyGroupId(),
        entity.getName(),
        entity.getOwnerOrganizationId(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }
}
