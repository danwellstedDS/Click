package com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.tenantgovernance.domain.aggregates.CustomerAccount;
import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.entity.CustomerAccountEntity;

public final class CustomerAccountMapper {
  private CustomerAccountMapper() {}

  public static CustomerAccount toDomain(CustomerAccountEntity entity) {
    return CustomerAccount.create(
        entity.getId(),
        entity.getType(),
        entity.getName(),
        entity.getOrganizationId(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }
}
