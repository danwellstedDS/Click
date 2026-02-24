package persistence.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import persistence.entity.ContractEntity;

public interface ContractJpaRepository extends JpaRepository<ContractEntity, UUID> {
  List<ContractEntity> findByCustomerAccountId(UUID customerAccountId);
}
