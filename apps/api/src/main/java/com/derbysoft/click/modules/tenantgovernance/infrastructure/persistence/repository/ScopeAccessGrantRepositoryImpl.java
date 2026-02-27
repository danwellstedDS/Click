package com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.repository;

import com.derbysoft.click.modules.tenantgovernance.domain.ScopeAccessGrantRepository;
import com.derbysoft.click.modules.tenantgovernance.domain.entities.ScopeAccessGrant;
import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.GrantRole;
import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.entity.ScopeAccessGrantEntity;
import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.mapper.ScopeAccessGrantMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ScopeAccessGrantRepositoryImpl implements ScopeAccessGrantRepository {
  private final ScopeAccessGrantJpaRepository scopeAccessGrantJpaRepository;

  public ScopeAccessGrantRepositoryImpl(ScopeAccessGrantJpaRepository scopeAccessGrantJpaRepository) {
    this.scopeAccessGrantJpaRepository = scopeAccessGrantJpaRepository;
  }

  @Override
  public List<ScopeAccessGrant> findByOrganizationId(UUID organizationId) {
    return scopeAccessGrantJpaRepository.findByOrganizationId(organizationId).stream()
        .map(ScopeAccessGrantMapper::toDomain)
        .toList();
  }

  @Override
  public List<ScopeAccessGrant> findByScopeId(UUID scopeId) {
    return scopeAccessGrantJpaRepository.findByScopeId(scopeId).stream()
        .map(ScopeAccessGrantMapper::toDomain)
        .toList();
  }

  @Override
  public ScopeAccessGrant create(UUID organizationId, UUID scopeId, GrantRole role) {
    ScopeAccessGrantEntity entity = new ScopeAccessGrantEntity(organizationId, scopeId, role);
    return ScopeAccessGrantMapper.toDomain(scopeAccessGrantJpaRepository.saveAndFlush(entity));
  }
}
