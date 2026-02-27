package com.derbysoft.click.modules.tenantgovernance.domain.entities;

import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.ScopeType;
import java.time.Instant;
import java.util.UUID;

public record AccessScope(
    UUID id,
    ScopeType type,
    UUID propertyGroupId,
    UUID propertyId,
    UUID portfolioId,
    Instant createdAt
) {}
