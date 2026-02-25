package persistence.mapper;

import domain.Property;
import persistence.entity.PropertyEntity;

public final class PropertyMapper {
  private PropertyMapper() {
  }

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
