package com.derbysoft.click.modules.normalisation.domain.events;

import java.time.Instant;
import java.util.UUID;

public record CanonicalBatchRebuilt(
    UUID batchId,
    UUID snapshotId,
    UUID tenantId,
    String mappingVersion,
    Instant occurredAt
) {}
