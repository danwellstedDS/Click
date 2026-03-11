package com.derbysoft.click.modules.normalisation.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.normalisation.domain.aggregates.CanonicalBatch;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.BatchStatus;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.MappingVersion;
import com.derbysoft.click.modules.normalisation.infrastructure.persistence.entity.CanonicalBatchEntity;
import org.springframework.stereotype.Component;

@Component
public class CanonicalBatchMapper {

    public CanonicalBatchEntity toEntity(CanonicalBatch batch) {
        CanonicalBatchEntity entity = new CanonicalBatchEntity();
        entity.setId(batch.getId());
        entity.setSourceSnapshotId(batch.getSourceSnapshotId());
        entity.setIntegrationId(batch.getIntegrationId());
        entity.setTenantId(batch.getTenantId());
        entity.setAccountId(batch.getAccountId());
        entity.setMappingVersion(batch.getMappingVersion().value());
        entity.setStatus(batch.getStatus().name());
        entity.setFactCount(batch.getFactCount());
        entity.setQuarantinedCount(batch.getQuarantinedCount());
        entity.setChecksum(batch.getChecksum());
        entity.setProducedAt(batch.getProducedAt());
        entity.setFailedAt(batch.getFailedAt());
        entity.setFailureReason(batch.getFailureReason());
        entity.setCreatedAt(batch.getCreatedAt());
        entity.setUpdatedAt(batch.getUpdatedAt());
        return entity;
    }

    public CanonicalBatch toDomain(CanonicalBatchEntity entity) {
        return CanonicalBatch.reconstitute(
            entity.getId(), entity.getSourceSnapshotId(), entity.getIntegrationId(),
            entity.getTenantId(), entity.getAccountId(),
            new MappingVersion(entity.getMappingVersion()),
            BatchStatus.valueOf(entity.getStatus()),
            entity.getFactCount(), entity.getQuarantinedCount(), entity.getChecksum(),
            entity.getProducedAt(), entity.getFailedAt(), entity.getFailureReason(),
            entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }
}
