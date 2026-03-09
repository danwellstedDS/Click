package com.derbysoft.click.modules.campaignexecution.domain;

import com.derbysoft.click.modules.campaignexecution.domain.aggregates.ExecutionIncident;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecutionIncidentRepository {
    Optional<ExecutionIncident> findById(UUID id);
    Optional<ExecutionIncident> findByIdempotencyKey(String key);
    Optional<ExecutionIncident> findByRevisionIdAndItemIdAndFailureClass(
        UUID revisionId, UUID itemId, String failureClassKey);
    Optional<ExecutionIncident> findByRevisionIdAndItemId(UUID revisionId, UUID itemId);
    List<ExecutionIncident> findOpenByTenantId(UUID tenantId);
    List<ExecutionIncident> findEscalatedByTenantId(UUID tenantId);
    ExecutionIncident save(ExecutionIncident incident);
}
