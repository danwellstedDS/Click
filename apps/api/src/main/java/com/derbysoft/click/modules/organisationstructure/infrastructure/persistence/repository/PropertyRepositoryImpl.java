package com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.repository;

import com.derbysoft.click.modules.organisationstructure.domain.PropertyRepository;
import com.derbysoft.click.modules.organisationstructure.domain.entities.Property;
import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.entity.PropertyEntity;
import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.mapper.PropertyMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PropertyRepositoryImpl implements PropertyRepository {
  private final PropertyJpaRepository propertyJpaRepository;

  public PropertyRepositoryImpl(PropertyJpaRepository propertyJpaRepository) {
    this.propertyJpaRepository = propertyJpaRepository;
  }

  @Override
  public Optional<Property> findById(UUID id) {
    return propertyJpaRepository.findById(id).map(PropertyMapper::toDomain);
  }

  @Override
  public List<Property> findAllByPropertyGroupId(UUID propertyGroupId) {
    return propertyJpaRepository.findAllByPropertyGroupId(propertyGroupId).stream()
        .map(PropertyMapper::toDomain)
        .toList();
  }

  @Override
  public Property create(UUID propertyGroupId, String name, boolean isActive, String externalPropertyRef) {
    PropertyEntity entity = new PropertyEntity(propertyGroupId, name, isActive, externalPropertyRef);
    return PropertyMapper.toDomain(propertyJpaRepository.saveAndFlush(entity));
  }

  @Override
  public void deactivate(UUID id) {
    propertyJpaRepository.findById(id).ifPresent(e -> {
      e.setIsActive(false);
      propertyJpaRepository.saveAndFlush(e);
    });
  }
}
