package com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.organisationstructure.domain.aggregates.PropertyGroup;
import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.entity.PropertyGroupEntity;

public final class PropertyGroupMapper {
  private PropertyGroupMapper() {}

  public static PropertyGroup toDomain(PropertyGroupEntity entity) {
    return PropertyGroup.create(
        entity.getId(),
        entity.getParentId(),
        entity.getName(),
        entity.getTimezone(),
        entity.getCurrency(),
        entity.getPrimaryOrgId(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }
}
