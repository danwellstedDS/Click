package com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.tenantgovernance.domain.aggregates.Contract;
import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.entity.ContractEntity;

public final class ContractMapper {
  private ContractMapper() {}

  public static Contract toDomain(ContractEntity entity) {
    return Contract.create(
        entity.getId(),
        entity.getCustomerAccountId(),
        entity.getStatus(),
        entity.getStartDate(),
        entity.getEndDate(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }
}
