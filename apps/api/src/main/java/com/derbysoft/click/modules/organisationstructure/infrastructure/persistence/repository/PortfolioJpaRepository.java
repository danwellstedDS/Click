package com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.repository;

import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.entity.PortfolioEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioJpaRepository extends JpaRepository<PortfolioEntity, UUID> {
  List<PortfolioEntity> findAllByPropertyGroupId(UUID propertyGroupId);
}
