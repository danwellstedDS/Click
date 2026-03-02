package com.derbysoft.click.modules.identityaccess.infrastructure.persistence.repository;

import com.derbysoft.click.modules.identityaccess.infrastructure.persistence.entity.TenantMembershipEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantMembershipJpaRepository extends JpaRepository<TenantMembershipEntity, UUID> {
  List<TenantMembershipEntity> findByUserId(UUID userId);
  List<TenantMembershipEntity> findByTenantId(UUID tenantId);
  Optional<TenantMembershipEntity> findByUserIdAndTenantId(UUID userId, UUID tenantId);
}
