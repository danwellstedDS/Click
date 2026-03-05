package com.derbysoft.click.modules.googleadsmanagement.domain.events;

import java.time.Instant;
import java.util.UUID;

public record BindingRemoved(UUID bindingId, UUID connectionId, UUID tenantId, String customerId, Instant occurredAt) {}
