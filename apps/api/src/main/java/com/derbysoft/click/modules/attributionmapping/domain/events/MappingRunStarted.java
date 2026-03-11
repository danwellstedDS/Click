package com.derbysoft.click.modules.attributionmapping.domain.events;

import java.time.Instant;
import java.util.UUID;

public record MappingRunStarted(
    UUID runId,
    UUID canonicalBatchId,
    UUID tenantId,
    String ruleSetVersion,
    String overrideSetVersion,
    Instant occurredAt
) {}
