package domain.repository;

import domain.Contract;
import domain.ContractStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContractRepository {
  Optional<Contract> findById(UUID id);
  List<Contract> findByCustomerAccountId(UUID customerAccountId);
  Contract create(UUID customerAccountId, ContractStatus status, LocalDate startDate);
}
