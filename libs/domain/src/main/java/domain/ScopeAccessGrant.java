package domain;

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
