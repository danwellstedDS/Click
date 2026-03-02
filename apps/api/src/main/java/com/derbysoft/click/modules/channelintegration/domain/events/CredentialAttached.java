package com.derbysoft.click.modules.channelintegration.domain.events;

import com.derbysoft.click.modules.channelintegration.domain.valueobjects.CredentialRef;
import java.time.Instant;
import java.util.UUID;

public record CredentialAttached(
    UUID integrationId,
    CredentialRef ref,
    Instant occurredAt
) {}
