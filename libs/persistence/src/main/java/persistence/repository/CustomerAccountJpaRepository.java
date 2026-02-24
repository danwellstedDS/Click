package persistence.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import persistence.entity.CustomerAccountEntity;

public interface CustomerAccountJpaRepository extends JpaRepository<CustomerAccountEntity, UUID> {
}
