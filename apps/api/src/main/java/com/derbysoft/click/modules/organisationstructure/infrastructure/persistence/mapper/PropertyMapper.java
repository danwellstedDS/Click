package com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.organisationstructure.domain.entities.Property;
import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.entity.PropertyEntity;

public final class PropertyMapper {
  private PropertyMapper() {}

  public static Property toDomain(PropertyEntity entity) {
    return Property.create(
        entity.getId(),
        entity.getPropertyGroupId(),
        entity.getName(),
        entity.isActive(),
        entity.getExternalPropertyRef(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }
}
