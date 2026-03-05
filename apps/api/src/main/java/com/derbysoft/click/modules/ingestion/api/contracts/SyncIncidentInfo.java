package com.derbysoft.click.modules.ingestion.api.contracts;

import java.time.Instant;
import java.util.UUID;

public record SyncIncidentInfo(
    UUID id,
    String idempotencyKey,
    UUID tenantId,
    String failureClass,
    String status,
    int consecutiveFailures,
    Instant lastFailedAt,
    String acknowledgedBy,
    String ackReason
) {}
