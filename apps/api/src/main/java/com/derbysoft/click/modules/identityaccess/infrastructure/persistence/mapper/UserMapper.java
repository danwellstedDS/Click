package com.derbysoft.click.modules.identityaccess.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.identityaccess.domain.aggregates.User;
import com.derbysoft.click.modules.identityaccess.infrastructure.persistence.entity.UserEntity;

public final class UserMapper {
  private UserMapper() {}

  public static User toDomain(UserEntity entity) {
    return User.create(
        entity.getId(),
        entity.getEmail(),
        entity.getPasswordHash(),
        entity.getName(),
        entity.isActive(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }
}
