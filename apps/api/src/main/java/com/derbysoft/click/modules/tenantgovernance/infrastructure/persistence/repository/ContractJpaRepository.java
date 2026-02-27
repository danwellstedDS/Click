package com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.repository;

import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.entity.ContractEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractJpaRepository extends JpaRepository<ContractEntity, UUID> {
  List<ContractEntity> findByCustomerAccountId(UUID customerAccountId);
}
