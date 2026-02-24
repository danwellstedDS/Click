package persistence.mapper;

import domain.ContractCoverage;
import persistence.entity.ContractCoverageEntity;

public final class ContractCoverageMapper {
  private ContractCoverageMapper() {
  }

  public static ContractCoverage toDomain(ContractCoverageEntity entity) {
    return new ContractCoverage(
        entity.getId(),
        entity.getContractId(),
        entity.getScopeId(),
        entity.getCreatedAt()
    );
  }
}
