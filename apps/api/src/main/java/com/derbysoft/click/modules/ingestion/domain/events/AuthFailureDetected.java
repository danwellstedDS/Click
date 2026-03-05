package com.derbysoft.click.modules.ingestion.domain.events;

import java.time.Instant;
import java.util.UUID;

public record AuthFailureDetected(
    UUID integrationId,
    String accountId,
    UUID tenantId,
    String reason,
    Instant occurredAt
) {}
