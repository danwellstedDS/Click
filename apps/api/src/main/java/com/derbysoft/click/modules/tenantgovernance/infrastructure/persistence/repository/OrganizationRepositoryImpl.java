package com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.repository;

import com.derbysoft.click.modules.tenantgovernance.domain.OrganizationRepository;
import com.derbysoft.click.modules.tenantgovernance.domain.aggregates.Organization;
import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.OrganizationType;
import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.entity.OrganizationEntity;
import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.mapper.OrganizationMapper;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class OrganizationRepositoryImpl implements OrganizationRepository {
  private final OrganizationJpaRepository organizationJpaRepository;

  public OrganizationRepositoryImpl(OrganizationJpaRepository organizationJpaRepository) {
    this.organizationJpaRepository = organizationJpaRepository;
  }

  @Override
  public Optional<Organization> findById(UUID id) {
    return organizationJpaRepository.findById(id).map(OrganizationMapper::toDomain);
  }

  @Override
  public Organization create(String name, OrganizationType type) {
    OrganizationEntity entity = new OrganizationEntity(name, type);
    return OrganizationMapper.toDomain(organizationJpaRepository.saveAndFlush(entity));
  }
}
