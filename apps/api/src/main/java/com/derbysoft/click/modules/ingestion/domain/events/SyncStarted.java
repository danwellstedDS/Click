package com.derbysoft.click.modules.ingestion.domain.events;

import com.derbysoft.click.modules.ingestion.domain.valueobjects.TriggerType;
import java.time.Instant;
import java.util.UUID;

public record SyncStarted(
    UUID jobId,
    String idempotencyKey,
    UUID tenantId,
    TriggerType triggerType,
    Instant occurredAt
) {}
