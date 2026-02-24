package persistence.mapper;

import domain.CustomerAccount;
import persistence.entity.CustomerAccountEntity;

public final class CustomerAccountMapper {
  private CustomerAccountMapper() {
  }

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
