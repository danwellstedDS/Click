package com.derbysoft.click.modules.ingestion.domain.events;

import java.time.Instant;
import java.util.UUID;

public record SyncIncidentEscalated(
    UUID incidentId,
    String idempotencyKey,
    UUID tenantId,
    int consecutiveFailures,
    Instant occurredAt
) {}
