package com.derbysoft.click.modules.channelintegration.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.channelintegration.domain.aggregates.IntegrationInstance;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.CadenceType;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.CredentialRef;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.IntegrationHealth;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.IntegrationStatus;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.SyncSchedule;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.SyncStatus;
import com.derbysoft.click.modules.channelintegration.infrastructure.persistence.entity.IntegrationInstanceEntity;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class IntegrationInstanceMapper {

    public IntegrationInstance toDomain(IntegrationInstanceEntity entity) {
        CredentialRef credentialRef = entity.getCredentialRefId() != null
            ? new CredentialRef(entity.getCredentialRefId())
            : null;

        CadenceType cadenceType = CadenceType.valueOf(entity.getCadenceType());
        SyncSchedule syncSchedule = switch (cadenceType) {
            case MANUAL -> SyncSchedule.manual(entity.getScheduleTimezone());
            case CRON -> SyncSchedule.cron(entity.getCronExpression(), entity.getScheduleTimezone());
            case INTERVAL -> SyncSchedule.interval(entity.getIntervalMinutes(), entity.getScheduleTimezone());
        };

        IntegrationHealth health = new IntegrationHealth(
            entity.getLastSyncAt(),
            SyncStatus.valueOf(entity.getLastSyncStatus()),
            entity.getLastSuccessAt(),
            entity.getLastErrorCode(),
            entity.getLastErrorMessage(),
            entity.getConsecutiveFailures(),
            entity.getStatusReason()
        );

        return IntegrationInstance.reconstitute(
            entity.getId(),
            entity.getTenantId(),
            Channel.valueOf(entity.getChannel()),
            entity.getConnectionKey(),
            IntegrationStatus.fromString(entity.getStatus()),
            credentialRef,
            syncSchedule,
            health,
            entity.getCredentialAttachedAt(),
            entity.getUpdatedBy(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public IntegrationInstanceEntity toEntity(IntegrationInstance domain) {
        UUID credentialRefId = domain.getCredentialRef() != null
            ? domain.getCredentialRef().credentialId()
            : null;

        SyncSchedule schedule = domain.getSyncSchedule();
        IntegrationHealth health = domain.getHealth();

        return new IntegrationInstanceEntity(
            domain.getId(),
            domain.getTenantId(),
            domain.getChannel().name(),
            domain.getConnectionKey(),
            domain.getStatus().name(),
            credentialRefId,
            schedule.cadenceType().name(),
            schedule.cronExpression(),
            schedule.intervalMinutes(),
            schedule.timezone(),
            health.lastSyncAt(),
            health.lastSyncStatus().name(),
            health.lastSuccessAt(),
            health.lastErrorCode(),
            health.lastErrorMessage(),
            health.consecutiveFailures(),
            health.statusReason(),
            domain.getCredentialAttachedAt(),
            domain.getUpdatedBy()
        );
    }
}
