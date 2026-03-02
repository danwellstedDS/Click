package com.derbysoft.click.modules.channelintegration.api.events;

import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import java.time.Instant;
import java.util.UUID;

/**
 * Cross-BC integration event shape for SyncRequested.
 * Published by BC4 when a sync run is initiated; consumed by BC7 (Ingestion).
 */
public record SyncRequestedEvent(
    UUID integrationId,
    UUID tenantId,
    Channel channel,
    UUID credentialId,
    UUID syncRunId,
    Instant occurredAt
) {}
