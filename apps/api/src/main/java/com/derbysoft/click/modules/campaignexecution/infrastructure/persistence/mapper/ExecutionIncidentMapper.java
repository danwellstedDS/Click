package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.campaignexecution.domain.aggregates.ExecutionIncident;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.IncidentStatus;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.entity.ExecutionIncidentEntity;
import org.springframework.stereotype.Component;

@Component
public class ExecutionIncidentMapper {

    public ExecutionIncident toDomain(ExecutionIncidentEntity entity) {
        return ExecutionIncident.reconstitute(
            entity.getId(),
            entity.getIdempotencyKey(),
            entity.getTenantId(),
            FailureClass.valueOf(entity.getFailureClass()),
            IncidentStatus.valueOf(entity.getStatus()),
            entity.getConsecutiveFailures(),
            entity.getFirstFailedAt(),
            entity.getLastFailedAt(),
            entity.getAcknowledgedBy(),
            entity.getAckReason(),
            entity.getAcknowledgedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public ExecutionIncidentEntity toEntity(ExecutionIncident domain) {
        return new ExecutionIncidentEntity(
            domain.getId(),
            domain.getIdempotencyKey(),
            domain.getTenantId(),
            domain.getFailureClass().name(),
            domain.getStatus().name(),
            domain.getConsecutiveFailures(),
            domain.getFirstFailedAt(),
            domain.getLastFailedAt(),
            domain.getAcknowledgedBy(),
            domain.getAckReason(),
            domain.getAcknowledgedAt()
        );
    }
}
