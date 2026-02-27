package com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.repository;

import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.entity.ContractCoverageEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractCoverageJpaRepository extends JpaRepository<ContractCoverageEntity, UUID> {}
