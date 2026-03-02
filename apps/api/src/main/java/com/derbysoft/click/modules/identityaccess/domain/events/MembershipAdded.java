package com.derbysoft.click.modules.identityaccess.domain.events;

import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;
import java.time.Instant;
import java.util.UUID;

public record MembershipAdded(UUID membershipId, UUID userId, UUID tenantId, Role role, Instant occurredAt) {}
