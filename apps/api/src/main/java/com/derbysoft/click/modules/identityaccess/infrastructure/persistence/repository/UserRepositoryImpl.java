package com.derbysoft.click.modules.identityaccess.infrastructure.persistence.repository;

import com.derbysoft.click.modules.identityaccess.domain.UserRepository;
import com.derbysoft.click.modules.identityaccess.domain.aggregates.User;
import com.derbysoft.click.modules.identityaccess.infrastructure.persistence.entity.UserEntity;
import com.derbysoft.click.modules.identityaccess.infrastructure.persistence.mapper.UserMapper;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {
  private final UserJpaRepository userJpaRepository;

  public UserRepositoryImpl(UserJpaRepository userJpaRepository) {
    this.userJpaRepository = userJpaRepository;
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return userJpaRepository.findByEmail(email).map(UserMapper::toDomain);
  }

  @Override
  public Optional<User> findById(UUID id) {
    return userJpaRepository.findById(id).map(UserMapper::toDomain);
  }

  @Override
  public User create(String email, String passwordHash) {
    UserEntity entity = new UserEntity(email, passwordHash);
    return UserMapper.toDomain(userJpaRepository.saveAndFlush(entity));
  }

  @Override
  public void deleteById(UUID id) {
    userJpaRepository.deleteById(id);
  }
}
