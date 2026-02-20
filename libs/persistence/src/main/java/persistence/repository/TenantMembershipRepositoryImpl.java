package persistence.repository;

import domain.Role;
import domain.TenantMembership;
import domain.repository.TenantMembershipRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import persistence.entity.TenantMembershipEntity;
import persistence.mapper.TenantMembershipMapper;

@Repository
public class TenantMembershipRepositoryImpl implements TenantMembershipRepository {
  private final TenantMembershipJpaRepository tenantMembershipJpaRepository;

  public TenantMembershipRepositoryImpl(TenantMembershipJpaRepository tenantMembershipJpaRepository) {
    this.tenantMembershipJpaRepository = tenantMembershipJpaRepository;
  }

  @Override
  public List<TenantMembership> findByUserId(UUID userId) {
    return tenantMembershipJpaRepository.findByUserId(userId).stream()
        .map(TenantMembershipMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<TenantMembership> findByUserAndTenant(UUID userId, UUID tenantId) {
    return tenantMembershipJpaRepository.findByUserIdAndTenantId(userId, tenantId)
        .map(TenantMembershipMapper::toDomain);
  }

  @Override
  public TenantMembership create(UUID userId, UUID tenantId, Role role) {
    TenantMembershipEntity entity = new TenantMembershipEntity(userId, tenantId, role);
    return TenantMembershipMapper.toDomain(tenantMembershipJpaRepository.save(entity));
  }
}
