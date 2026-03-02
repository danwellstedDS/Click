package com.derbysoft.click.modules.channelintegration.domain.events;

import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import java.time.Instant;
import java.util.UUID;

public record IntegrationCreated(
    UUID integrationId,
    UUID tenantId,
    Channel channel,
    Instant occurredAt
) {}
