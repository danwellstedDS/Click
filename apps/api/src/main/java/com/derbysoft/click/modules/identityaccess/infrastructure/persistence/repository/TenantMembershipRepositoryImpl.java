package com.derbysoft.click.modules.identityaccess.infrastructure.persistence.repository;

import com.derbysoft.click.modules.identityaccess.domain.TenantMembershipRepository;
import com.derbysoft.click.modules.identityaccess.domain.entities.TenantMembership;
import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;
import com.derbysoft.click.modules.identityaccess.infrastructure.persistence.entity.TenantMembershipEntity;
import com.derbysoft.click.modules.identityaccess.infrastructure.persistence.mapper.TenantMembershipMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class TenantMembershipRepositoryImpl implements TenantMembershipRepository {
  private final TenantMembershipJpaRepository jpaRepository;

  public TenantMembershipRepositoryImpl(TenantMembershipJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public List<TenantMembership> findByUserId(UUID userId) {
    return jpaRepository.findByUserId(userId).stream()
        .map(TenantMembershipMapper::toDomain)
        .toList();
  }

  @Override
  public List<TenantMembership> findByTenantId(UUID tenantId) {
    return jpaRepository.findByTenantId(tenantId).stream()
        .map(TenantMembershipMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<TenantMembership> findByUserAndTenant(UUID userId, UUID tenantId) {
    return jpaRepository.findByUserIdAndTenantId(userId, tenantId)
        .map(TenantMembershipMapper::toDomain);
  }

  @Override
  public TenantMembership create(UUID membershipId, UUID userId, UUID tenantId, Role role) {
    TenantMembershipEntity entity = new TenantMembershipEntity(membershipId, userId, tenantId, role.name());
    return TenantMembershipMapper.toDomain(jpaRepository.saveAndFlush(entity));
  }

  @Override
  public TenantMembership updateRole(UUID userId, UUID tenantId, Role role) {
    jpaRepository.updateRole(userId, tenantId, role.name());
    return jpaRepository.findByUserIdAndTenantId(userId, tenantId)
        .map(TenantMembershipMapper::toDomain)
        .orElseThrow();
  }
}
