package persistence.mapper;

import domain.RefreshToken;
import persistence.entity.RefreshTokenEntity;

public final class RefreshTokenMapper {
  private RefreshTokenMapper() {
  }

  public static RefreshToken toDomain(RefreshTokenEntity entity) {
    return RefreshToken.create(
        entity.getId(),
        entity.getUserId(),
        entity.getTokenHash(),
        entity.getExpiresAt(),
        entity.getCreatedAt()
    );
  }
}
