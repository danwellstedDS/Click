package com.derbysoft.click.modules.attributionmapping.api.contracts;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MappedFactInfo(
    UUID id,
    UUID mappingRunId,
    UUID canonicalFactId,
    UUID tenantId,
    UUID resolvedOrgNodeId,
    String resolvedScopeType,
    String confidenceBand,
    BigDecimal confidenceScore,
    String resolutionReasonCode,
    String ruleSetVersion,
    boolean overrideApplied,
    Instant mappedAt
) {}
