package com.derbysoft.click.modules.ingestion.domain;

import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncIncident;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SyncIncidentRepository {
    Optional<SyncIncident> findById(UUID id);
    Optional<SyncIncident> findByIdempotencyKey(String key);
    List<SyncIncident> findOpenByTenantId(UUID tenantId);
    List<SyncIncident> findEscalatedByTenantId(UUID tenantId);
    SyncIncident save(SyncIncident incident);
}
