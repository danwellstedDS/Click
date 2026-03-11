package com.derbysoft.click.modules.attributionmapping.interfaces.http.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MappedFactResponse(
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
