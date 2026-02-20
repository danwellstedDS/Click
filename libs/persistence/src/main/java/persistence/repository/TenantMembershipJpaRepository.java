package persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import persistence.entity.TenantMembershipEntity;

public interface TenantMembershipJpaRepository extends JpaRepository<TenantMembershipEntity, UUID> {
  List<TenantMembershipEntity> findByUserId(UUID userId);
  Optional<TenantMembershipEntity> findByUserIdAndTenantId(UUID userId, UUID tenantId);
}
