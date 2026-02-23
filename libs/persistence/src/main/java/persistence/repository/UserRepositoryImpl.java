package persistence.repository;

import domain.User;
import domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import persistence.entity.UserEntity;
import persistence.mapper.UserMapper;

@Repository
public class UserRepositoryImpl implements UserRepository {
  private final UserJpaRepository userJpaRepository;
  private final TenantMembershipJpaRepository tenantMembershipJpaRepository;

  public UserRepositoryImpl(
      UserJpaRepository userJpaRepository,
      TenantMembershipJpaRepository tenantMembershipJpaRepository
  ) {
    this.userJpaRepository = userJpaRepository;
    this.tenantMembershipJpaRepository = tenantMembershipJpaRepository;
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
  public List<User> findAllByTenantId(UUID tenantId) {
    List<UUID> userIds = tenantMembershipJpaRepository.findAllByTenantId(tenantId).stream()
        .map(membership -> membership.getUserId())
        .toList();
    return userJpaRepository.findAllById(userIds).stream()
        .map(UserMapper::toDomain)
        .toList();
  }

  @Override
  public void deleteById(UUID id) {
    userJpaRepository.deleteById(id);
  }
}
