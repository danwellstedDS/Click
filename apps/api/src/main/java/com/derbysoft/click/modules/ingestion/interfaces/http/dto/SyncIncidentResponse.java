package com.derbysoft.click.modules.ingestion.interfaces.http.dto;

import java.time.Instant;
import java.util.UUID;

public record SyncIncidentResponse(
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
