package persistence.mapper;

import domain.Organization;
import persistence.entity.OrganizationEntity;

public final class OrganizationMapper {
  private OrganizationMapper() {
  }

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
