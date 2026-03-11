package com.derbysoft.click.modules.ingestion.domain.events;

import com.derbysoft.click.modules.ingestion.domain.valueobjects.FailureClass;
import java.time.Instant;
import java.util.UUID;

public record SyncFailed(
    UUID jobId,
    String idempotencyKey,
    UUID tenantId,
    UUID integrationId,
    FailureClass failureClass,
    String reason,
    Instant occurredAt
) {}
