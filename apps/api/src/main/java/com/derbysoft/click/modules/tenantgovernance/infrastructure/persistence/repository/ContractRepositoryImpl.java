package com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.repository;

import com.derbysoft.click.modules.tenantgovernance.domain.ContractRepository;
import com.derbysoft.click.modules.tenantgovernance.domain.aggregates.Contract;
import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.ContractStatus;
import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.entity.ContractEntity;
import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.mapper.ContractMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ContractRepositoryImpl implements ContractRepository {
  private final ContractJpaRepository contractJpaRepository;

  public ContractRepositoryImpl(ContractJpaRepository contractJpaRepository) {
    this.contractJpaRepository = contractJpaRepository;
  }

  @Override
  public Optional<Contract> findById(UUID id) {
    return contractJpaRepository.findById(id).map(ContractMapper::toDomain);
  }

  @Override
  public List<Contract> findByCustomerAccountId(UUID customerAccountId) {
    return contractJpaRepository.findByCustomerAccountId(customerAccountId).stream()
        .map(ContractMapper::toDomain)
        .toList();
  }

  @Override
  public Contract create(UUID customerAccountId, ContractStatus status, LocalDate startDate) {
    ContractEntity entity = new ContractEntity(customerAccountId, status, startDate);
    return ContractMapper.toDomain(contractJpaRepository.saveAndFlush(entity));
  }
}
