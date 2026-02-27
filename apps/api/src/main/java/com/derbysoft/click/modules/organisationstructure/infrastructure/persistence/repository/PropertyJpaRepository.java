package com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.repository;

import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.entity.PropertyEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyJpaRepository extends JpaRepository<PropertyEntity, UUID> {
  List<PropertyEntity> findAllByPropertyGroupId(UUID propertyGroupId);
}
