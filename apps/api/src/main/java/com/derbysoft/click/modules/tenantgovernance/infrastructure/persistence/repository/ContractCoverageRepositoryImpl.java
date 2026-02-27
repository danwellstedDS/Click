package com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.repository;

import com.derbysoft.click.modules.tenantgovernance.domain.entities.ContractCoverage;
import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.entity.ContractCoverageEntity;
import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.mapper.ContractCoverageMapper;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ContractCoverageRepositoryImpl {
  private final ContractCoverageJpaRepository contractCoverageJpaRepository;

  public ContractCoverageRepositoryImpl(ContractCoverageJpaRepository contractCoverageJpaRepository) {
    this.contractCoverageJpaRepository = contractCoverageJpaRepository;
  }

  public Optional<ContractCoverage> findById(UUID id) {
    return contractCoverageJpaRepository.findById(id).map(ContractCoverageMapper::toDomain);
  }

  public ContractCoverage create(UUID contractId, UUID scopeId) {
    ContractCoverageEntity entity = new ContractCoverageEntity(contractId, scopeId);
    return ContractCoverageMapper.toDomain(contractCoverageJpaRepository.saveAndFlush(entity));
  }
}
