package com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.repository;

import com.derbysoft.click.modules.attributionmapping.api.contracts.MappedFactInfo;
import com.derbysoft.click.modules.attributionmapping.api.contracts.MappingRunInfo;
import com.derbysoft.click.modules.attributionmapping.api.ports.AttributionQueryPort;
import com.derbysoft.click.modules.attributionmapping.domain.MappingRunRepository;
import com.derbysoft.click.modules.attributionmapping.domain.aggregates.MappingRun;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappedFactEntity;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappingRunEntity;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.mapper.MappingRunMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;

/**
 * Implements both {@link MappingRunRepository} (BC9 domain port) and
 * {@link AttributionQueryPort} (BC9 public API port). Dual-interface pattern.
 */
public class MappingRunRepositoryImpl implements MappingRunRepository, AttributionQueryPort {

    private final MappingRunJpaRepository runJpaRepository;
    private final MappedFactJpaRepository factJpaRepository;
    private final MappingRunMapper mapper;

    public MappingRunRepositoryImpl(
        MappingRunJpaRepository runJpaRepository,
        MappedFactJpaRepository factJpaRepository,
        MappingRunMapper mapper
    ) {
        this.runJpaRepository = runJpaRepository;
        this.factJpaRepository = factJpaRepository;
        this.mapper = mapper;
    }

    // MappingRunRepository

    @Override
    public MappingRun save(MappingRun run) {
        MappingRunEntity entity = mapper.toEntity(run);
        MappingRunEntity saved = runJpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<MappingRun> findById(UUID id) {
        return runJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<MappingRun> findByCanonicalBatchIdAndRuleSetVersionAndOverrideSetVersion(
        UUID canonicalBatchId, String ruleSetVersion, String overrideSetVersion
    ) {
        return runJpaRepository.findByCanonicalBatchIdAndRuleSetVersionAndOverrideSetVersion(
            canonicalBatchId, ruleSetVersion, overrideSetVersion
        ).map(mapper::toDomain);
    }

    // AttributionQueryPort

    @Override
    public Optional<MappingRunInfo> findRunById(UUID runId) {
        return runJpaRepository.findById(runId).map(this::toInfo);
    }

    @Override
    public List<MappingRunInfo> listRuns(UUID tenantId, int page, int size) {
        return runJpaRepository.findByTenantId(tenantId, PageRequest.of(page, size))
            .stream().map(this::toInfo).toList();
    }

    @Override
    public List<MappedFactInfo> listFacts(UUID runId, int page, int size) {
        return factJpaRepository.findByMappingRunId(runId, PageRequest.of(page, size))
            .stream().map(this::toFactInfo).toList();
    }

    @Override
    public List<MappedFactInfo> listLowConfidence(UUID runId, int page, int size) {
        return factJpaRepository.findByMappingRunIdAndConfidenceBandIn(
            runId, List.of("LOW", "UNRESOLVED"), PageRequest.of(page, size)
        ).stream().map(this::toFactInfo).toList();
    }

    private MappingRunInfo toInfo(MappingRunEntity e) {
        return new MappingRunInfo(
            e.getId(), e.getCanonicalBatchId(), e.getTenantId(),
            e.getRuleSetVersion(), e.getOverrideSetVersion(), e.getStatus(),
            e.getMappedCount(), e.getLowConfidenceCount(), e.getUnresolvedCount(),
            e.getStartedAt(), e.getCompletedAt(), e.getFailedAt(), e.getFailureReason(),
            e.getCreatedAt()
        );
    }

    private MappedFactInfo toFactInfo(MappedFactEntity e) {
        return new MappedFactInfo(
            e.getId(), e.getMappingRunId(), e.getCanonicalFactId(), e.getTenantId(),
            e.getResolvedOrgNodeId(), e.getResolvedScopeType(),
            e.getConfidenceBand(), e.getConfidenceScore(),
            e.getResolutionReasonCode(), e.getRuleSetVersion(),
            e.isOverrideApplied(), e.getMappedAt()
        );
    }
}
