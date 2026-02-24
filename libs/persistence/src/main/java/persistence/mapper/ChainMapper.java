package persistence.mapper;

import domain.Chain;
import persistence.entity.ChainEntity;

public final class ChainMapper {
  private ChainMapper() {
  }

  public static Chain toDomain(ChainEntity entity) {
    return Chain.create(
        entity.getId(),
        entity.getName(),
        entity.getTimezone(),
        entity.getCurrency(),
        entity.getPrimaryOrgId(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }
}
