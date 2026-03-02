package com.derbysoft.click.modules.channelintegration.domain.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when a sync run fails. Consumed from BC7 (Ingestion) via the in-process event bus.
 * Also used internally when BC4 receives a failure notification and marks the instance Broken.
 */
public record SyncFailed(
    UUID integrationId,
    UUID syncRunId,
    String reason,
    Instant occurredAt
) {}
