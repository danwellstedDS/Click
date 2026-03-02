package com.derbysoft.click.modules.identityaccess.domain.entities;

import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;
import java.time.Instant;
import java.util.UUID;

public record TenantMembership(
    UUID id,
    UUID userId,
    UUID tenantId,    // = PropertyGroup.id
    Role role,
    Instant createdAt
) {}
