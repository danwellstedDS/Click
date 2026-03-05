package com.derbysoft.click.modules.ingestion.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncJob;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.DateWindow;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.SyncJobStatus;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.TriggerType;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.entity.SyncJobEntity;
import org.springframework.stereotype.Component;

@Component
public class SyncJobMapper {

    public SyncJob toDomain(SyncJobEntity entity) {
        return SyncJob.reconstitute(
            entity.getId(),
            entity.getIntegrationId(),
            entity.getTenantId(),
            entity.getAccountId(),
            entity.getReportType(),
            new DateWindow(entity.getDateFrom(), entity.getDateTo()),
            TriggerType.valueOf(entity.getTriggerType()),
            entity.getIdempotencyKey(),
            SyncJobStatus.valueOf(entity.getStatus()),
            entity.getAttempts(),
            entity.getMaxAttempts(),
            entity.getLastAttemptAt(),
            entity.getLeaseExpiresAt(),
            entity.getNextAttemptAfter(),
            entity.getFailureClass() != null ? FailureClass.valueOf(entity.getFailureClass()) : null,
            entity.getFailureReason(),
            entity.getTriggeredBy(),
            entity.getTriggerReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public SyncJobEntity toEntity(SyncJob domain) {
        return new SyncJobEntity(
            domain.getId(),
            domain.getIntegrationId(),
            domain.getTenantId(),
            domain.getAccountId(),
            domain.getReportType(),
            domain.getDateWindow().from(),
            domain.getDateWindow().to(),
            domain.getTriggerType().name(),
            domain.getIdempotencyKey(),
            domain.getStatus().name(),
            domain.getAttempts(),
            domain.getMaxAttempts(),
            domain.getLastAttemptAt(),
            domain.getLeaseExpiresAt(),
            domain.getNextAttemptAfter(),
            domain.getFailureClass() != null ? domain.getFailureClass().name() : null,
            domain.getFailureReason(),
            domain.getTriggeredBy(),
            domain.getTriggerReason()
        );
    }
}
