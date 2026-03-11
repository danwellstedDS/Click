package com.derbysoft.click.modules.normalisation.infrastructure.persistence.repository;

import com.derbysoft.click.modules.normalisation.api.contracts.CanonicalBatchInfo;
import com.derbysoft.click.modules.normalisation.api.contracts.CanonicalFactInfo;
import com.derbysoft.click.modules.normalisation.api.contracts.NormalisationQualityStats;
import com.derbysoft.click.modules.normalisation.api.ports.NormalisationQueryPort;
import com.derbysoft.click.modules.normalisation.domain.CanonicalBatchRepository;
import com.derbysoft.click.modules.normalisation.domain.aggregates.CanonicalBatch;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.MappingVersion;
import com.derbysoft.click.modules.normalisation.infrastructure.persistence.entity.CanonicalBatchEntity;
import com.derbysoft.click.modules.normalisation.infrastructure.persistence.entity.CanonicalFactEntity;
import com.derbysoft.click.modules.normalisation.infrastructure.persistence.mapper.CanonicalBatchMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;

/**
 * Implements both {@link CanonicalBatchRepository} (BC8 domain port) and
 * {@link NormalisationQueryPort} (BC8 public API port). Dual-interface pattern.
 */
public class CanonicalBatchRepositoryImpl implements CanonicalBatchRepository, NormalisationQueryPort {

    private final CanonicalBatchJpaRepository batchJpaRepository;
    private final CanonicalFactJpaRepository factJpaRepository;
    private final CanonicalBatchMapper mapper;

    public CanonicalBatchRepositoryImpl(
        CanonicalBatchJpaRepository batchJpaRepository,
        CanonicalFactJpaRepository factJpaRepository,
        CanonicalBatchMapper mapper
    ) {
        this.batchJpaRepository = batchJpaRepository;
        this.factJpaRepository = factJpaRepository;
        this.mapper = mapper;
    }

    // CanonicalBatchRepository

    @Override
    public CanonicalBatch save(CanonicalBatch batch) {
        CanonicalBatchEntity entity = mapper.toEntity(batch);
        CanonicalBatchEntity saved = batchJpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<CanonicalBatch> findById(UUID id) {
        return batchJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<CanonicalBatch> findBySourceSnapshotIdAndMappingVersion(UUID snapshotId, MappingVersion version) {
        return batchJpaRepository.findBySourceSnapshotIdAndMappingVersion(snapshotId, version.value())
            .map(mapper::toDomain);
    }

    // NormalisationQueryPort

    @Override
    public Optional<CanonicalBatchInfo> findBatchById(UUID batchId) {
        return batchJpaRepository.findById(batchId).map(this::toInfo);
    }

    @Override
    public List<CanonicalBatchInfo> listBatches(UUID tenantId, String statusFilter, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        List<CanonicalBatchEntity> entities = (statusFilter != null && !statusFilter.isBlank())
            ? batchJpaRepository.findByTenantIdAndStatus(tenantId, statusFilter.toUpperCase(), pageable)
            : batchJpaRepository.findByTenantId(tenantId, pageable);
        return entities.stream().map(this::toInfo).toList();
    }

    @Override
    public List<CanonicalFactInfo> listFacts(UUID batchId, int page, int size) {
        return factJpaRepository.findByCanonicalBatchId(batchId, PageRequest.of(page, size))
            .stream().map(this::toFactInfo).toList();
    }

    @Override
    public NormalisationQualityStats getQualityStats(UUID batchId) {
        long total = factJpaRepository.countByCanonicalBatchId(batchId);
        long quarantined = factJpaRepository.countByCanonicalBatchIdAndQuarantinedTrue(batchId);
        Map<String, Long> flagBreakdown = new HashMap<>();
        factJpaRepository.findByCanonicalBatchId(batchId, PageRequest.of(0, Integer.MAX_VALUE))
            .stream()
            .filter(CanonicalFactEntity::isQuarantined)
            .forEach(f -> {
                if (f.getQualityFlags() != null) {
                    for (String flag : f.getQualityFlags()) {
                        flagBreakdown.merge(flag, 1L, Long::sum);
                    }
                }
            });
        return new NormalisationQualityStats(total, quarantined, flagBreakdown);
    }

    private CanonicalBatchInfo toInfo(CanonicalBatchEntity e) {
        return new CanonicalBatchInfo(
            e.getId(), e.getSourceSnapshotId(), e.getIntegrationId(), e.getTenantId(),
            e.getAccountId(), e.getMappingVersion(), e.getStatus(),
            e.getFactCount(), e.getQuarantinedCount(), e.getChecksum(),
            e.getProducedAt(), e.getFailedAt(), e.getFailureReason(), e.getCreatedAt()
        );
    }

    private CanonicalFactInfo toFactInfo(CanonicalFactEntity e) {
        List<String> flags = e.getQualityFlags() != null ? Arrays.asList(e.getQualityFlags()) : List.of();
        return new CanonicalFactInfo(
            e.getId(), e.getCanonicalBatchId(), e.getSourceSnapshotId(), e.getTenantId(),
            e.getChannel(), e.getIntegrationId(), e.getCustomerAccountId(),
            e.getCampaignId(), e.getCampaignName(), e.getReportDate(),
            e.getImpressions(), e.getClicks(), e.getCostMicros(), e.getCostAmount(), e.getConversions(),
            e.getMappingVersion(), e.getReconciliationKey(), flags, e.isQuarantined(), e.getIngestedAt()
        );
    }
}
