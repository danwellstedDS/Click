package com.derbysoft.click.modules.ingestion.domain.events;

import java.time.Instant;
import java.util.UUID;

public record SyncIncidentReopened(
    UUID incidentId,
    String idempotencyKey,
    UUID tenantId,
    Instant occurredAt
) {}
