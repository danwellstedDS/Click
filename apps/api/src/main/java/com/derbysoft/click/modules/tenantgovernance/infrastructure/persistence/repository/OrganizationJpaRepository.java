package com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.repository;

import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.entity.OrganizationEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationJpaRepository extends JpaRepository<OrganizationEntity, UUID> {}
