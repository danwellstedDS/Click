package com.derbysoft.click.modules.ingestion.domain.events;

import java.time.Instant;
import java.util.UUID;

public record SyncSucceeded(
    UUID jobId,
    String idempotencyKey,
    UUID tenantId,
    Instant occurredAt
) {}
