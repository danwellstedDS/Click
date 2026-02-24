package persistence.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import persistence.entity.OrganizationEntity;

public interface OrganizationJpaRepository extends JpaRepository<OrganizationEntity, UUID> {
}
