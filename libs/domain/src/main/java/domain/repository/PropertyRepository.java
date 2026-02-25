package domain.repository;

import domain.Property;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository {
  Optional<Property> findById(UUID id);
  List<Property> findAllByPropertyGroupId(UUID propertyGroupId);
  Property create(UUID propertyGroupId, String name);
}
