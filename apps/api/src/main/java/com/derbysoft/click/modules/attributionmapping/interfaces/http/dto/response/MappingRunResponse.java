package com.derbysoft.click.modules.attributionmapping.interfaces.http.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MappingRunResponse(
    UUID id,
    UUID canonicalBatchId,
    UUID tenantId,
    String ruleSetVersion,
    String overrideSetVersion,
    String status,
    int mappedCount,
    int lowConfidenceCount,
    int unresolvedCount,
    Instant startedAt,
    Instant completedAt,
    Instant failedAt,
    String failureReason,
    Instant createdAt
) {}
