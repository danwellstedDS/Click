package com.derbysoft.click.modules.attributionmapping.interfaces.http.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MappingOverrideResponse(
    UUID id,
    UUID tenantId,
    String scopeType,
    String customerAccountId,
    String campaignId,
    UUID targetOrgNodeId,
    String targetScopeType,
    String reason,
    String actor,
    String status,
    Instant removedAt,
    String removedReason,
    Instant createdAt
) {}
