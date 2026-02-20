package persistence.mapper;

import domain.User;
import persistence.entity.UserEntity;

public final class UserMapper {
  private UserMapper() {
  }

  public static User toDomain(UserEntity entity) {
    return User.create(
        entity.getId(),
        entity.getEmail(),
        entity.getPasswordHash(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }
}
