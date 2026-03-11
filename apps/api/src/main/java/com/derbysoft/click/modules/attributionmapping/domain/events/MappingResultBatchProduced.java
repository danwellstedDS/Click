package com.derbysoft.click.modules.attributionmapping.domain.events;

import java.time.Instant;
import java.util.UUID;

public record MappingResultBatchProduced(
    UUID runId,
    UUID canonicalBatchId,
    UUID tenantId,
    String ruleSetVersion,
    String overrideSetVersion,
    int mappedCount,
    int lowConfidenceCount,
    int unresolvedCount,
    Instant occurredAt
) {}
