package com.derbysoft.click.modules.channelintegration.domain.events;

import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.CredentialRef;
import java.time.Instant;
import java.util.UUID;

public record SyncRequested(
    UUID integrationId,
    UUID tenantId,
    Channel channel,
    CredentialRef credentialRef,
    UUID syncRunId,
    Instant occurredAt
) {}
