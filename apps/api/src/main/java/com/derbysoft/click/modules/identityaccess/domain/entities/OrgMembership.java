package com.derbysoft.click.modules.identityaccess.domain.entities;

import java.time.Instant;
import java.util.UUID;

public record OrgMembership(
    UUID id,
    UUID userId,
    UUID organizationId,
    boolean isOrgAdmin,
    Instant createdAt
) {}
