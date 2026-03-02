package com.derbysoft.click.modules.identityaccess.domain.events;

import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;
import java.time.Instant;
import java.util.UUID;

public record UserAuthenticated(UUID userId, String email, UUID tenantId, Role role, Instant occurredAt) {}
