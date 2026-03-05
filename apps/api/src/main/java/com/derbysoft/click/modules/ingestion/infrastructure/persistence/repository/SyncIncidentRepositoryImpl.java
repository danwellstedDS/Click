package com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository;

import com.derbysoft.click.modules.ingestion.api.contracts.SyncIncidentInfo;
import com.derbysoft.click.modules.ingestion.api.contracts.SyncJobInfo;
import com.derbysoft.click.modules.ingestion.api.ports.IngestionQueryPort;
import com.derbysoft.click.modules.ingestion.domain.SyncIncidentRepository;
import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncIncident;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.mapper.SyncIncidentMapper;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.mapper.SyncJobMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implements both {@link SyncIncidentRepository} (BC7 domain port) and
 * {@link IngestionQueryPort} (BC7 public API port). Dual-interface pattern — same as
 * {@code GoogleConnectionRepositoryImpl} in googleadsmanagement.
 */
public class SyncIncidentRepositoryImpl implements SyncIncidentRepository, IngestionQueryPort {

    private final SyncIncidentJpaRepository incidentJpaRepository;
    private final SyncJobJpaRepository syncJobJpaRepository;
    private final SyncIncidentMapper incidentMapper;
    private final SyncJobMapper jobMapper;

    public SyncIncidentRepositoryImpl(
        SyncIncidentJpaRepository incidentJpaRepository,
        SyncJobJpaRepository syncJobJpaRepository,
        SyncIncidentMapper incidentMapper,
        SyncJobMapper jobMapper
    ) {
        this.incidentJpaRepository = incidentJpaRepository;
        this.syncJobJpaRepository = syncJobJpaRepository;
        this.incidentMapper = incidentMapper;
        this.jobMapper = jobMapper;
    }

    // SyncIncidentRepository

    @Override
    public Optional<SyncIncident> findById(UUID id) {
        return incidentJpaRepository.findById(id).map(incidentMapper::toDomain);
    }

    @Override
    public Optional<SyncIncident> findByIdempotencyKey(String key) {
        return incidentJpaRepository.findByIdempotencyKey(key).map(incidentMapper::toDomain);
    }

    @Override
    public List<SyncIncident> findOpenByTenantId(UUID tenantId) {
        return incidentJpaRepository.findByTenantIdAndStatusIn(tenantId, List.of("OPEN", "REOPENED"))
            .stream()
            .map(incidentMapper::toDomain)
            .toList();
    }

    @Override
    public List<SyncIncident> findEscalatedByTenantId(UUID tenantId) {
        return incidentJpaRepository
            .findByTenantIdAndStatusAndAcknowledgedAtIsNull(tenantId, "ESCALATED")
            .stream()
            .map(incidentMapper::toDomain)
            .toList();
    }

    @Override
    public SyncIncident save(SyncIncident incident) {
        var entity = incidentMapper.toEntity(incident);
        var saved = incidentJpaRepository.saveAndFlush(entity);
        return incidentMapper.toDomain(saved);
    }

    // IngestionQueryPort

    @Override
    public List<SyncJobInfo> listJobHistory(UUID integrationId) {
        return syncJobJpaRepository.findByIntegrationId(integrationId).stream()
            .map(e -> new SyncJobInfo(
                e.getId(), e.getIntegrationId(), e.getAccountId(), e.getReportType(),
                e.getDateFrom(), e.getDateTo(), e.getStatus(), e.getAttempts(),
                e.getTriggerType(), e.getFailureClass(), e.getCreatedAt()
            ))
            .toList();
    }

    @Override
    public List<SyncIncidentInfo> listOpenIncidents(UUID tenantId) {
        return incidentJpaRepository
            .findByTenantIdAndStatusIn(tenantId, List.of("OPEN", "REOPENED"))
            .stream()
            .map(e -> new SyncIncidentInfo(
                e.getId(), e.getIdempotencyKey(), e.getTenantId(), e.getFailureClass(),
                e.getStatus(), e.getConsecutiveFailures(), e.getLastFailedAt(),
                e.getAcknowledgedBy(), e.getAckReason()
            ))
            .toList();
    }

    @Override
    public List<SyncIncidentInfo> listEscalatedIncidents(UUID tenantId) {
        return incidentJpaRepository
            .findByTenantIdAndStatusAndAcknowledgedAtIsNull(tenantId, "ESCALATED")
            .stream()
            .map(e -> new SyncIncidentInfo(
                e.getId(), e.getIdempotencyKey(), e.getTenantId(), e.getFailureClass(),
                e.getStatus(), e.getConsecutiveFailures(), e.getLastFailedAt(),
                e.getAcknowledgedBy(), e.getAckReason()
            ))
            .toList();
    }
}
