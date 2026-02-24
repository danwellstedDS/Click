package persistence.mapper;

import domain.Contract;
import persistence.entity.ContractEntity;

public final class ContractMapper {
  private ContractMapper() {
  }

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
