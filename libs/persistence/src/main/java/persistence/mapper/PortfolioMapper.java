package persistence.mapper;

import domain.Portfolio;
import persistence.entity.PortfolioEntity;

public final class PortfolioMapper {
  private PortfolioMapper() {
  }

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
