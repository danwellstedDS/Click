package com.derbysoft.click.modules.normalisation.domain.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CanonicalBatchProduced(
    UUID batchId,
    UUID tenantId,
    String channel,
    List<UUID> sourceSnapshotIds,
    String mappingVersion,
    int factCount,
    int quarantinedCount,
    String checksum,
    Instant occurredAt
) {}
