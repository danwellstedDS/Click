package com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.attributionmapping.domain.aggregates.MappingRun;
import com.derbysoft.click.modules.attributionmapping.domain.valueobjects.RunStatus;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappingRunEntity;
import org.springframework.stereotype.Component;

@Component
public class MappingRunMapper {

    public MappingRunEntity toEntity(MappingRun run) {
        MappingRunEntity entity = new MappingRunEntity();
        entity.setId(run.getId());
        entity.setCanonicalBatchId(run.getCanonicalBatchId());
        entity.setTenantId(run.getTenantId());
        entity.setRuleSetVersion(run.getRuleSetVersion());
        entity.setOverrideSetVersion(run.getOverrideSetVersion());
        entity.setStatus(run.getStatus().name());
        entity.setMappedCount(run.getMappedCount());
        entity.setLowConfidenceCount(run.getLowConfidenceCount());
        entity.setUnresolvedCount(run.getUnresolvedCount());
        entity.setStartedAt(run.getStartedAt());
        entity.setCompletedAt(run.getCompletedAt());
        entity.setFailedAt(run.getFailedAt());
        entity.setFailureReason(run.getFailureReason());
        entity.setCreatedAt(run.getCreatedAt());
        entity.setUpdatedAt(run.getUpdatedAt());
        return entity;
    }

    public MappingRun toDomain(MappingRunEntity entity) {
        return MappingRun.reconstitute(
            entity.getId(), entity.getCanonicalBatchId(), entity.getTenantId(),
            entity.getRuleSetVersion(), entity.getOverrideSetVersion(),
            RunStatus.valueOf(entity.getStatus()),
            entity.getMappedCount(), entity.getLowConfidenceCount(), entity.getUnresolvedCount(),
            entity.getStartedAt(), entity.getCompletedAt(), entity.getFailedAt(), entity.getFailureReason(),
            entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }
}
