package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository;

import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.entity.ExecutionIncidentEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionIncidentJpaRepository extends JpaRepository<ExecutionIncidentEntity, UUID> {
    Optional<ExecutionIncidentEntity> findByIdempotencyKey(String idempotencyKey);
    List<ExecutionIncidentEntity> findByTenantIdAndStatusIn(UUID tenantId, List<String> statuses);
    List<ExecutionIncidentEntity> findByTenantIdAndStatusAndAcknowledgedAtIsNull(
        UUID tenantId, String status);
}
