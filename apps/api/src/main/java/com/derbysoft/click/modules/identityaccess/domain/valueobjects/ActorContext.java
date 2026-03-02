package com.derbysoft.click.modules.identityaccess.domain.valueobjects;

import java.util.UUID;

public record ActorContext(UUID userId, UUID tenantId, String email, Role role) {}
