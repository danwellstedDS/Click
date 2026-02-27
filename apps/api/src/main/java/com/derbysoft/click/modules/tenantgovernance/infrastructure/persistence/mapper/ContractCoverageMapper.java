package com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.tenantgovernance.domain.entities.ContractCoverage;
import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.entity.ContractCoverageEntity;

public final class ContractCoverageMapper {
  private ContractCoverageMapper() {}

  public static ContractCoverage toDomain(ContractCoverageEntity entity) {
    return new ContractCoverage(
        entity.getId(),
        entity.getContractId(),
        entity.getScopeId(),
        entity.getCreatedAt()
    );
  }
}
