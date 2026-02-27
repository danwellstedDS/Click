package com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.repository;

import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.entity.PropertyGroupEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyGroupJpaRepository extends JpaRepository<PropertyGroupEntity, UUID> {
  Optional<PropertyGroupEntity> findByPrimaryOrgId(UUID primaryOrgId);
}
