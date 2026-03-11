package com.derbysoft.click.modules.normalisation.domain.events;

import java.time.Instant;
import java.util.UUID;

public record CanonicalBatchFailed(
    UUID batchId,
    UUID snapshotId,
    UUID tenantId,
    String failureReason,
    Instant occurredAt
) {}
