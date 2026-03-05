package com.derbysoft.click.modules.ingestion.domain.events;

import com.derbysoft.click.modules.ingestion.domain.valueobjects.FailureClass;
import java.time.Instant;
import java.util.UUID;

public record SyncIncidentOpened(
    UUID incidentId,
    String idempotencyKey,
    UUID tenantId,
    FailureClass failureClass,
    Instant occurredAt
) {}
