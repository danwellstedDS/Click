package com.derbysoft.click.modules.tenantgovernance.domain.entities;

import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.GrantRole;
import java.time.Instant;
import java.util.UUID;

public record ScopeAccessGrant(
    UUID id,
    UUID organizationId,
    UUID scopeId,
    GrantRole role,
    Instant validFrom,
    Instant validTo,
    Instant createdAt
) {}
