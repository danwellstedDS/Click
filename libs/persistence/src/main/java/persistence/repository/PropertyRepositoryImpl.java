package persistence.repository;

import domain.Property;
import domain.repository.PropertyRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import persistence.entity.PropertyEntity;
import persistence.mapper.PropertyMapper;

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
  public Property create(UUID propertyGroupId, String name) {
    PropertyEntity entity = new PropertyEntity(propertyGroupId, name);
    return PropertyMapper.toDomain(propertyJpaRepository.saveAndFlush(entity));
  }
}
