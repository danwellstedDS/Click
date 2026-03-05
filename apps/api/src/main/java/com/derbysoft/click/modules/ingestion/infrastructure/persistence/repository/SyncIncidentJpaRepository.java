package com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository;

import com.derbysoft.click.modules.ingestion.infrastructure.persistence.entity.SyncIncidentEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncIncidentJpaRepository extends JpaRepository<SyncIncidentEntity, UUID> {

    Optional<SyncIncidentEntity> findByIdempotencyKey(String idempotencyKey);

    List<SyncIncidentEntity> findByTenantIdAndStatusIn(UUID tenantId, List<String> statuses);

    List<SyncIncidentEntity> findByTenantIdAndStatusAndAcknowledgedAtIsNull(
        UUID tenantId, String status);
}
