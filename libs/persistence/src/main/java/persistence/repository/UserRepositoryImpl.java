package persistence.repository;

import domain.User;
import domain.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import persistence.entity.UserEntity;
import persistence.mapper.UserMapper;

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
    return UserMapper.toDomain(userJpaRepository.save(entity));
  }
}
