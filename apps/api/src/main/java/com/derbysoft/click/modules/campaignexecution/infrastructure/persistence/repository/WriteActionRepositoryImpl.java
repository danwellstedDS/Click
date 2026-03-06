package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository;

import com.derbysoft.click.modules.campaignexecution.api.contracts.ExecutionIncidentSummary;
import com.derbysoft.click.modules.campaignexecution.api.contracts.PlanItemInfo;
import com.derbysoft.click.modules.campaignexecution.api.contracts.PlanRevisionInfo;
import com.derbysoft.click.modules.campaignexecution.api.ports.CampaignManagementQueryPort;
import com.derbysoft.click.modules.campaignexecution.domain.WriteActionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.WriteAction;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.TriggerType;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper.WriteActionMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implements both {@link WriteActionRepository} (BC6 domain port) and
 * {@link CampaignManagementQueryPort} (BC6 public API port). Dual-interface pattern —
 * same as {@code SyncIncidentRepositoryImpl} in ingestion.
 */
public class WriteActionRepositoryImpl implements WriteActionRepository, CampaignManagementQueryPort {

    private final WriteActionJpaRepository writeActionJpaRepository;
    private final PlanRevisionJpaRepository planRevisionJpaRepository;
    private final PlanItemJpaRepository planItemJpaRepository;
    private final ExecutionIncidentJpaRepository incidentJpaRepository;
    private final WriteActionMapper writeActionMapper;

    public WriteActionRepositoryImpl(
        WriteActionJpaRepository writeActionJpaRepository,
        PlanRevisionJpaRepository planRevisionJpaRepository,
        PlanItemJpaRepository planItemJpaRepository,
        ExecutionIncidentJpaRepository incidentJpaRepository,
        WriteActionMapper writeActionMapper
    ) {
        this.writeActionJpaRepository = writeActionJpaRepository;
        this.planRevisionJpaRepository = planRevisionJpaRepository;
        this.planItemJpaRepository = planItemJpaRepository;
        this.incidentJpaRepository = incidentJpaRepository;
        this.writeActionMapper = writeActionMapper;
    }

    // WriteActionRepository

    @Override
    public Optional<WriteAction> findById(UUID id) {
        return writeActionJpaRepository.findById(id).map(writeActionMapper::toDomain);
    }

    @Override
    public Optional<WriteAction> findByIdempotencyKey(String key) {
        return writeActionJpaRepository.findByIdempotencyKey(key).map(writeActionMapper::toDomain);
    }

    @Override
    public List<WriteAction> findPendingActions(Instant now) {
        return writeActionJpaRepository.findPendingActions(now)
            .stream().map(writeActionMapper::toDomain).toList();
    }

    @Override
    public List<WriteAction> findRunningActionsWithExpiredLease(Instant now) {
        return writeActionJpaRepository.findRunningActionsWithExpiredLease(now)
            .stream().map(writeActionMapper::toDomain).toList();
    }

    @Override
    public List<WriteAction> findByRevisionId(UUID revisionId) {
        return writeActionJpaRepository.findByRevisionId(revisionId)
            .stream().map(writeActionMapper::toDomain).toList();
    }

    @Override
    public long countManualTriggersSince(UUID tenantId, List<TriggerType> triggerTypes,
                                          Instant since) {
        List<String> triggerTypeStrings = triggerTypes.stream().map(TriggerType::name).toList();
        return writeActionJpaRepository.countByTenantIdAndTriggerTypeInAndCreatedAtAfter(
            tenantId, triggerTypeStrings, since);
    }

    @Override
    public WriteAction save(WriteAction action) {
        var entity = writeActionMapper.toEntity(action);
        var saved = writeActionJpaRepository.saveAndFlush(entity);
        return writeActionMapper.toDomain(saved);
    }

    // CampaignManagementQueryPort

    @Override
    public List<PlanRevisionInfo> listAppliedRevisions(UUID tenantId) {
        return planRevisionJpaRepository.findByTenantIdAndStatus(tenantId, "APPLIED").stream()
            .map(e -> new PlanRevisionInfo(
                e.getId(), e.getPlanId(), e.getTenantId(), e.getRevisionNumber(),
                e.getStatus(), e.getPublishedBy(), e.getPublishedAt(), e.getCreatedAt()
            ))
            .toList();
    }

    @Override
    public Optional<PlanRevisionInfo> findRevisionById(UUID revisionId) {
        return planRevisionJpaRepository.findById(revisionId)
            .map(e -> new PlanRevisionInfo(
                e.getId(), e.getPlanId(), e.getTenantId(), e.getRevisionNumber(),
                e.getStatus(), e.getPublishedBy(), e.getPublishedAt(), e.getCreatedAt()
            ));
    }

    @Override
    public List<PlanItemInfo> listSucceededItems(UUID revisionId) {
        return planItemJpaRepository.findByRevisionIdAndStatusIn(revisionId, List.of("SUCCEEDED"))
            .stream()
            .map(e -> new PlanItemInfo(
                e.getId(), e.getRevisionId(), e.getActionType(),
                e.getResourceType(), e.getResourceId(), e.getStatus(),
                e.getAttempts(), e.getUpdatedAt()
            ))
            .toList();
    }

    @Override
    public List<ExecutionIncidentSummary> listOpenIncidents(UUID tenantId) {
        return incidentJpaRepository.findByTenantIdAndStatusIn(tenantId, List.of("OPEN", "REOPENED"))
            .stream()
            .map(e -> new ExecutionIncidentSummary(
                e.getId(), e.getIdempotencyKey(), e.getTenantId(),
                e.getFailureClass(), e.getStatus(), e.getConsecutiveFailures(),
                e.getLastFailedAt(), e.getAcknowledgedBy(), e.getAckReason()
            ))
            .toList();
    }

    @Override
    public List<ExecutionIncidentSummary> listEscalatedIncidents(UUID tenantId) {
        return incidentJpaRepository.findByTenantIdAndStatusAndAcknowledgedAtIsNull(tenantId, "ESCALATED")
            .stream()
            .map(e -> new ExecutionIncidentSummary(
                e.getId(), e.getIdempotencyKey(), e.getTenantId(),
                e.getFailureClass(), e.getStatus(), e.getConsecutiveFailures(),
                e.getLastFailedAt(), e.getAcknowledgedBy(), e.getAckReason()
            ))
            .toList();
    }
}
