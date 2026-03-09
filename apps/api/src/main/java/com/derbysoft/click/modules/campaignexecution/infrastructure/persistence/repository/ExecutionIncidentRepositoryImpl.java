package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository;

import com.derbysoft.click.modules.campaignexecution.domain.ExecutionIncidentRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.ExecutionIncident;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper.ExecutionIncidentMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ExecutionIncidentRepositoryImpl implements ExecutionIncidentRepository {

    private final ExecutionIncidentJpaRepository jpaRepository;
    private final ExecutionIncidentMapper mapper;

    public ExecutionIncidentRepositoryImpl(ExecutionIncidentJpaRepository jpaRepository,
                                            ExecutionIncidentMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<ExecutionIncident> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<ExecutionIncident> findByIdempotencyKey(String key) {
        return jpaRepository.findByIdempotencyKey(key).map(mapper::toDomain);
    }

    @Override
    public Optional<ExecutionIncident> findByRevisionIdAndItemIdAndFailureClass(
        UUID revisionId, UUID itemId, String failureClassKey) {
        return jpaRepository.findByRevisionIdAndItemIdAndFailureClassKey(
            revisionId, itemId, failureClassKey).map(mapper::toDomain);
    }

    @Override
    public Optional<ExecutionIncident> findByRevisionIdAndItemId(UUID revisionId, UUID itemId) {
        return jpaRepository.findFirstByRevisionIdAndItemIdOrderByCreatedAtDesc(
            revisionId, itemId).map(mapper::toDomain);
    }

    @Override
    public List<ExecutionIncident> findOpenByTenantId(UUID tenantId) {
        return jpaRepository.findByTenantIdAndStatusIn(tenantId, List.of("OPEN", "REOPENED"))
            .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<ExecutionIncident> findEscalatedByTenantId(UUID tenantId) {
        return jpaRepository.findByTenantIdAndStatusAndAcknowledgedAtIsNull(tenantId, "ESCALATED")
            .stream().map(mapper::toDomain).toList();
    }

    @Override
    public ExecutionIncident save(ExecutionIncident incident) {
        var entity = mapper.toEntity(incident);
        var saved = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }
}
