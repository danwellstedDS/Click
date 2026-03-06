package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.campaignexecution.domain.entities.PlanItem;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.ApplyOrder;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.PlanItemStatus;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionType;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.entity.PlanItemEntity;
import org.springframework.stereotype.Component;

@Component
public class PlanItemMapper {

    public PlanItem toDomain(PlanItemEntity entity) {
        return PlanItem.reconstitute(
            entity.getId(),
            entity.getRevisionId(),
            entity.getTenantId(),
            PlanItemStatus.valueOf(entity.getStatus()),
            WriteActionType.valueOf(entity.getActionType()),
            entity.getResourceType(),
            entity.getResourceId(),
            entity.getPayload(),
            applyOrderFromInt(entity.getApplyOrder()),
            entity.getAttempts(),
            entity.getMaxAttempts(),
            entity.getLastAttemptAt(),
            entity.getNextAttemptAfter(),
            entity.getFailureClass() != null ? FailureClass.valueOf(entity.getFailureClass()) : null,
            entity.getFailureReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public PlanItemEntity toEntity(PlanItem domain) {
        return new PlanItemEntity(
            domain.getId(),
            domain.getRevisionId(),
            domain.getTenantId(),
            domain.getStatus().name(),
            domain.getActionType().name(),
            domain.getResourceType(),
            domain.getResourceId(),
            domain.getPayload(),
            domain.getApplyOrder().getOrder(),
            domain.getAttempts(),
            domain.getMaxAttempts(),
            domain.getLastAttemptAt(),
            domain.getNextAttemptAfter(),
            domain.getFailureClass() != null ? domain.getFailureClass().name() : null,
            domain.getFailureReason()
        );
    }

    private ApplyOrder applyOrderFromInt(int order) {
        for (ApplyOrder ao : ApplyOrder.values()) {
            if (ao.getOrder() == order) return ao;
        }
        throw new IllegalArgumentException("Unknown apply order: " + order);
    }
}
