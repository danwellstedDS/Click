package com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.repository;

import com.derbysoft.click.modules.organisationstructure.api.contracts.PropertyGroupInfo;
import com.derbysoft.click.modules.organisationstructure.api.ports.PropertyGroupQueryPort;
import com.derbysoft.click.modules.organisationstructure.domain.PropertyGroupRepository;
import com.derbysoft.click.modules.organisationstructure.domain.aggregates.PropertyGroup;
import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.entity.PropertyGroupEntity;
import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.mapper.PropertyGroupMapper;
import java.util.Optional;
import java.util.UUID;
public class PropertyGroupRepositoryImpl implements PropertyGroupRepository, PropertyGroupQueryPort {
  private final PropertyGroupJpaRepository propertyGroupJpaRepository;

  public PropertyGroupRepositoryImpl(PropertyGroupJpaRepository propertyGroupJpaRepository) {
    this.propertyGroupJpaRepository = propertyGroupJpaRepository;
  }

  @Override
  public Optional<PropertyGroup> findById(UUID id) {
    return propertyGroupJpaRepository.findById(id).map(PropertyGroupMapper::toDomain);
  }

  @Override
  public Optional<PropertyGroup> findByPrimaryOrgId(UUID primaryOrgId) {
    return propertyGroupJpaRepository.findByPrimaryOrgId(primaryOrgId).map(PropertyGroupMapper::toDomain);
  }

  @Override
  public PropertyGroup create(String name) {
    PropertyGroupEntity entity = new PropertyGroupEntity(name);
    return PropertyGroupMapper.toDomain(propertyGroupJpaRepository.saveAndFlush(entity));
  }

  @Override
  public Optional<PropertyGroupInfo> findInfoById(UUID id) {
    return propertyGroupJpaRepository.findById(id)
        .map(e -> new PropertyGroupInfo(e.getId(), e.getName(), e.getPrimaryOrgId()));
  }

  @Override
  public Optional<PropertyGroupInfo> findInfoByPrimaryOrgId(UUID primaryOrgId) {
    return propertyGroupJpaRepository.findByPrimaryOrgId(primaryOrgId)
        .map(e -> new PropertyGroupInfo(e.getId(), e.getName(), e.getPrimaryOrgId()));
  }
}
