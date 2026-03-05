package com.derbysoft.click.modules.ingestion.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncIncident;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.IncidentStatus;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.entity.SyncIncidentEntity;
import org.springframework.stereotype.Component;

@Component
public class SyncIncidentMapper {

    public SyncIncident toDomain(SyncIncidentEntity entity) {
        return SyncIncident.reconstitute(
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

    public SyncIncidentEntity toEntity(SyncIncident domain) {
        return new SyncIncidentEntity(
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
