package com.derbysoft.click.modules.tenantgovernance.domain;

import com.derbysoft.click.modules.tenantgovernance.domain.aggregates.CustomerAccount;
import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.CustomerType;
import java.util.Optional;
import java.util.UUID;

public interface CustomerAccountRepository {
  Optional<CustomerAccount> findById(UUID id);
  CustomerAccount create(CustomerType type, String name);
}
