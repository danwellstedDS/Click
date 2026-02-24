package domain.repository;

import domain.CustomerAccount;
import domain.CustomerType;
import java.util.Optional;
import java.util.UUID;

public interface CustomerAccountRepository {
  Optional<CustomerAccount> findById(UUID id);
  CustomerAccount create(CustomerType type, String name);
}
