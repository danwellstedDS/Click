package persistence.mapper;

import domain.PropertyGroup;
import persistence.entity.PropertyGroupEntity;

public final class PropertyGroupMapper {
  private PropertyGroupMapper() {
  }

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
