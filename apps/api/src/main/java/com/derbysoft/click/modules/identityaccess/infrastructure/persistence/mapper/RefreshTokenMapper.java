package com.derbysoft.click.modules.identityaccess.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.identityaccess.domain.entities.RefreshToken;
import com.derbysoft.click.modules.identityaccess.infrastructure.persistence.entity.RefreshTokenEntity;

public final class RefreshTokenMapper {
  private RefreshTokenMapper() {}

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
