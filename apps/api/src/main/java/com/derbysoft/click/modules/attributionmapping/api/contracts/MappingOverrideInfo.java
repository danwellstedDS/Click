package com.derbysoft.click.modules.attributionmapping.api.contracts;

import java.time.Instant;
import java.util.UUID;

public record MappingOverrideInfo(
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
