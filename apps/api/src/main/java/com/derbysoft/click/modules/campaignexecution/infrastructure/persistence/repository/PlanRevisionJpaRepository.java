package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository;

import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.entity.PlanRevisionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRevisionJpaRepository extends JpaRepository<PlanRevisionEntity, UUID> {
    List<PlanRevisionEntity> findByPlanId(UUID planId);
    Optional<PlanRevisionEntity> findFirstByTenantIdAndStatus(UUID tenantId, String status);
    List<PlanRevisionEntity> findByTenantIdAndStatus(UUID tenantId, String status);
}
