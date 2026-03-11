package com.derbysoft.click.modules.attributionmapping.domain.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LowConfidenceMappingDetected(
    UUID runId,
    UUID canonicalBatchId,
    UUID tenantId,
    int lowConfidenceCount,
    int unresolvedCount,
    List<UUID> lowConfidenceFactIds,
    Instant occurredAt
) {}
