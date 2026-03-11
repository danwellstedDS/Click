package com.derbysoft.click.modules.attributionmapping.domain.events;

import java.time.Instant;
import java.util.UUID;

public record MappingOverrideSet(
    UUID overrideId,
    UUID tenantId,
    String scopeType,
    String customerAccountId,
    String campaignId,
    UUID targetOrgNodeId,
    String actor,
    Instant occurredAt
) {}
