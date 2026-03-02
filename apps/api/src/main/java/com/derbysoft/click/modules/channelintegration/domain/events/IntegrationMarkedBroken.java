package com.derbysoft.click.modules.channelintegration.domain.events;

import java.time.Instant;
import java.util.UUID;

public record IntegrationMarkedBroken(
    UUID integrationId,
    String reason,
    Instant occurredAt
) {}
