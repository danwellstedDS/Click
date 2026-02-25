package domain.repository;

import domain.PropertyGroup;
import java.util.Optional;
import java.util.UUID;

public interface PropertyGroupRepository {
  Optional<PropertyGroup> findById(UUID id);
  Optional<PropertyGroup> findByPrimaryOrgId(UUID primaryOrgId);
  PropertyGroup create(String name);
}
