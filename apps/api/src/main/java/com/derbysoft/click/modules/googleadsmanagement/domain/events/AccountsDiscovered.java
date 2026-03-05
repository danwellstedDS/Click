package com.derbysoft.click.modules.googleadsmanagement.domain.events;

import java.time.Instant;
import java.util.UUID;

public record AccountsDiscovered(UUID connectionId, UUID tenantId, int accountCount, Instant occurredAt) {}
