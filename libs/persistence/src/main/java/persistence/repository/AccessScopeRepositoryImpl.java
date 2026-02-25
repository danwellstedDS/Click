package persistence.repository;

import domain.AccessScope;
import domain.ScopeType;
import domain.repository.AccessScopeRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import persistence.entity.AccessScopeEntity;
import persistence.mapper.AccessScopeMapper;

@Repository
public class AccessScopeRepositoryImpl implements AccessScopeRepository {
  private final AccessScopeJpaRepository accessScopeJpaRepository;

  public AccessScopeRepositoryImpl(AccessScopeJpaRepository accessScopeJpaRepository) {
    this.accessScopeJpaRepository = accessScopeJpaRepository;
  }

  @Override
  public List<AccessScope> findByPropertyGroupId(UUID propertyGroupId) {
    return accessScopeJpaRepository.findByPropertyGroupId(propertyGroupId).stream()
        .map(AccessScopeMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<AccessScope> findByPropertyGroupIdAndType(UUID propertyGroupId, ScopeType type) {
    return accessScopeJpaRepository.findByPropertyGroupIdAndType(propertyGroupId, type)
        .map(AccessScopeMapper::toDomain);
  }

  @Override
  public List<AccessScope> findByPropertyId(UUID propertyId) {
    return accessScopeJpaRepository.findByPropertyId(propertyId).stream()
        .map(AccessScopeMapper::toDomain)
        .toList();
  }

  @Override
  public AccessScope create(ScopeType type, UUID propertyGroupId, UUID propertyId, UUID portfolioId) {
    AccessScopeEntity entity = new AccessScopeEntity(type, propertyGroupId, propertyId, portfolioId);
    return AccessScopeMapper.toDomain(accessScopeJpaRepository.saveAndFlush(entity));
  }
}
