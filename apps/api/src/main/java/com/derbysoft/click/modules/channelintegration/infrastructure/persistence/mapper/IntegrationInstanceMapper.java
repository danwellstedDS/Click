package com.derbysoft.click.modules.channelintegration.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.channelintegration.domain.aggregates.IntegrationInstance;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.CredentialRef;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.IntegrationStatus;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.SyncSchedule;
import com.derbysoft.click.modules.channelintegration.infrastructure.persistence.entity.IntegrationInstanceEntity;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class IntegrationInstanceMapper {

    public IntegrationInstance toDomain(IntegrationInstanceEntity entity) {
        CredentialRef credentialRef = entity.getCredentialRefId() != null
            ? new CredentialRef(entity.getCredentialRefId())
            : null;

        return IntegrationInstance.reconstitute(
            entity.getId(),
            entity.getTenantId(),
            Channel.valueOf(entity.getChannel()),
            IntegrationStatus.fromString(entity.getStatus()),
            credentialRef,
            new SyncSchedule(entity.getSyncScheduleCron(), entity.getSyncScheduleTimezone()),
            null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public IntegrationInstanceEntity toEntity(IntegrationInstance domain) {
        UUID credentialRefId = domain.getCredentialRef() != null
            ? domain.getCredentialRef().credentialId()
            : null;

        IntegrationInstanceEntity entity = new IntegrationInstanceEntity(
            domain.getId(),
            domain.getTenantId(),
            domain.getChannel().name(),
            domain.getStatus().name(),
            credentialRefId,
            domain.getSyncSchedule().cron(),
            domain.getSyncSchedule().timezone()
        );
        return entity;
    }
}
