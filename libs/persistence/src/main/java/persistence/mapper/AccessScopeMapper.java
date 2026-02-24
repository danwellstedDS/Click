package persistence.mapper;

import domain.AccessScope;
import persistence.entity.AccessScopeEntity;

public final class AccessScopeMapper {
  private AccessScopeMapper() {
  }

  public static AccessScope toDomain(AccessScopeEntity entity) {
    return new AccessScope(
        entity.getId(),
        entity.getType(),
        entity.getChainId(),
        entity.getHotelId(),
        entity.getPortfolioId(),
        entity.getCreatedAt()
    );
  }
}
