package persistence.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import persistence.entity.ContractCoverageEntity;

public interface ContractCoverageJpaRepository extends JpaRepository<ContractCoverageEntity, UUID> {
}
