package com.derbysoft.click.modules.googleadsmanagement.domain.events;

import java.time.Instant;
import java.util.UUID;

public record ConnectionCreated(UUID connectionId, UUID tenantId, String managerId, Instant occurredAt) {}
