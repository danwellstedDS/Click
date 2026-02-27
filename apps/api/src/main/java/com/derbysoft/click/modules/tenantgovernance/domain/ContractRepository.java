package com.derbysoft.click.modules.tenantgovernance.domain;

import com.derbysoft.click.modules.tenantgovernance.domain.aggregates.Contract;
import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.ContractStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContractRepository {
  Optional<Contract> findById(UUID id);
  List<Contract> findByCustomerAccountId(UUID customerAccountId);
  Contract create(UUID customerAccountId, ContractStatus status, LocalDate startDate);
}
