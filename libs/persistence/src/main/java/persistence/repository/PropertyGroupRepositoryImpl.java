package persistence.repository;

import domain.PropertyGroup;
import domain.repository.PropertyGroupRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import persistence.entity.PropertyGroupEntity;
import persistence.mapper.PropertyGroupMapper;

@Repository
public class PropertyGroupRepositoryImpl implements PropertyGroupRepository {
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
}
