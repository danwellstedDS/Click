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
  public List<AccessScope> findByChainId(UUID chainId) {
    return accessScopeJpaRepository.findByChainId(chainId).stream()
        .map(AccessScopeMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<AccessScope> findByChainIdAndType(UUID chainId, ScopeType type) {
    return accessScopeJpaRepository.findByChainIdAndType(chainId, type)
        .map(AccessScopeMapper::toDomain);
  }

  @Override
  public List<AccessScope> findByHotelId(UUID hotelId) {
    return accessScopeJpaRepository.findByHotelId(hotelId).stream()
        .map(AccessScopeMapper::toDomain)
        .toList();
  }

  @Override
  public AccessScope create(ScopeType type, UUID chainId, UUID hotelId, UUID portfolioId) {
    AccessScopeEntity entity = new AccessScopeEntity(type, chainId, hotelId, portfolioId);
    return AccessScopeMapper.toDomain(accessScopeJpaRepository.saveAndFlush(entity));
  }
}
