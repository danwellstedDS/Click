package com.derbysoft.click.modules.attributionmapping.domain.events;

import java.time.Instant;
import java.util.UUID;

public record MappingRunFailed(
    UUID runId,
    UUID canonicalBatchId,
    UUID tenantId,
    String failureReason,
    Instant occurredAt
) {}
