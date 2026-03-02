package com.derbysoft.click.modules.channelintegration.domain.events;

import java.time.Instant;
import java.util.UUID;

public record CredentialDetached(
    UUID integrationId,
    Instant occurredAt
) {}
