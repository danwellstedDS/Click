package com.derbysoft.click.modules.identityaccess.infrastructure.persistence.repository;

import com.derbysoft.click.modules.identityaccess.infrastructure.persistence.entity.TenantMembershipEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TenantMembershipJpaRepository extends JpaRepository<TenantMembershipEntity, UUID> {
  List<TenantMembershipEntity> findByUserId(UUID userId);
  List<TenantMembershipEntity> findByTenantId(UUID tenantId);
  Optional<TenantMembershipEntity> findByUserIdAndTenantId(UUID userId, UUID tenantId);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE TenantMembershipEntity e SET e.role = :role WHERE e.userId = :userId AND e.tenantId = :tenantId")
  void updateRole(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId, @Param("role") String role);
}
