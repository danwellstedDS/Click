package com.derbysoft.click.modules.attributionmapping.domain.events;

import java.time.Instant;
import java.util.UUID;

public record MappingOverrideRemoved(
    UUID overrideId,
    UUID tenantId,
    String actor,
    String reason,
    Instant occurredAt
) {}
