package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.campaignexecution.domain.aggregates.WriteAction;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.TriggerType;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionStatus;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionType;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.entity.WriteActionEntity;
import org.springframework.stereotype.Component;

@Component
public class WriteActionMapper {

    public WriteAction toDomain(WriteActionEntity entity) {
        return WriteAction.reconstitute(
            entity.getId(),
            entity.getRevisionId(),
            entity.getItemId(),
            entity.getTenantId(),
            WriteActionType.valueOf(entity.getActionType()),
            entity.getIdempotencyKey(),
            WriteActionStatus.valueOf(entity.getStatus()),
            entity.getAttempts(),
            entity.getMaxAttempts(),
            entity.getLastAttemptAt(),
            entity.getLeaseExpiresAt(),
            entity.getNextAttemptAfter(),
            entity.getFailureClass() != null ? FailureClass.valueOf(entity.getFailureClass()) : null,
            entity.getFailureReason(),
            entity.getTargetCustomerId(),
            entity.getTriggeredBy(),
            TriggerType.valueOf(entity.getTriggerType()),
            entity.getTriggerReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public WriteActionEntity toEntity(WriteAction domain) {
        return new WriteActionEntity(
            domain.getId(),
            domain.getRevisionId(),
            domain.getItemId(),
            domain.getTenantId(),
            domain.getActionType().name(),
            domain.getIdempotencyKey(),
            domain.getStatus().name(),
            domain.getAttempts(),
            domain.getMaxAttempts(),
            domain.getLastAttemptAt(),
            domain.getLeaseExpiresAt(),
            domain.getNextAttemptAfter(),
            domain.getFailureClass() != null ? domain.getFailureClass().name() : null,
            domain.getFailureReason(),
            domain.getTargetCustomerId(),
            domain.getTriggeredBy(),
            domain.getTriggerType().name(),
            domain.getTriggerReason()
        );
    }
}
