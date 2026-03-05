package com.derbysoft.click.modules.ingestion.domain.events;

import java.time.Instant;
import java.util.UUID;

public record ManualTriggerRateLimited(
    UUID tenantId,
    long retryAfterSeconds,
    Instant occurredAt
) {}
