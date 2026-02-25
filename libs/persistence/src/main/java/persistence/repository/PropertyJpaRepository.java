package persistence.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import persistence.entity.PropertyEntity;

public interface PropertyJpaRepository extends JpaRepository<PropertyEntity, UUID> {
  List<PropertyEntity> findAllByPropertyGroupId(UUID propertyGroupId);
}
