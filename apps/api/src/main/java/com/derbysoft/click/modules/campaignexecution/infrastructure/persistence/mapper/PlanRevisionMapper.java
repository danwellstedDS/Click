package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.campaignexecution.domain.aggregates.PlanRevision;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.PlanRevisionStatus;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.entity.PlanRevisionEntity;
import org.springframework.stereotype.Component;

@Component
public class PlanRevisionMapper {

    public PlanRevision toDomain(PlanRevisionEntity entity) {
        return PlanRevision.reconstitute(
            entity.getId(),
            entity.getPlanId(),
            entity.getTenantId(),
            entity.getRevisionNumber(),
            PlanRevisionStatus.valueOf(entity.getStatus()),
            entity.getPublishedBy(),
            entity.getPublishedAt(),
            entity.getCancelledBy(),
            entity.getCancelReason(),
            entity.getCancelledAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public PlanRevisionEntity toEntity(PlanRevision domain) {
        return new PlanRevisionEntity(
            domain.getId(),
            domain.getPlanId(),
            domain.getTenantId(),
            domain.getRevisionNumber(),
            domain.getStatus().name(),
            domain.getPublishedBy(),
            domain.getPublishedAt(),
            domain.getCancelledBy(),
            domain.getCancelReason(),
            domain.getCancelledAt()
        );
    }
}
