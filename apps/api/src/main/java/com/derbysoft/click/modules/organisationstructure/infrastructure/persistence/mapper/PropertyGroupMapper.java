package com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.organisationstructure.domain.aggregates.PropertyGroup;
import com.derbysoft.click.modules.organisationstructure.domain.valueobjects.ChainStatus;
import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.entity.PropertyGroupEntity;

public final class PropertyGroupMapper {
  private PropertyGroupMapper() {}

  public static PropertyGroup toDomain(PropertyGroupEntity entity) {
    return PropertyGroup.reconstitute(
        entity.getId(),
        entity.getParentId(),
        entity.getName(),
        entity.getTimezone(),
        entity.getCurrency(),
        entity.getPrimaryOrgId(),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        ChainStatus.valueOf(entity.getStatus())
    );
  }

  public static PropertyGroupEntity toEntity(PropertyGroup domain) {
    return new PropertyGroupEntity(
        domain.getId(),
        domain.getParentId(),
        domain.getName(),
        domain.getTimezone(),
        domain.getCurrency(),
        domain.getPrimaryOrgId(),
        domain.getStatus().name(),
        domain.getCreatedAt(),
        domain.getUpdatedAt()
    );
  }
}
