package com.derbysoft.click.modules.googleadsmanagement.domain.events;

import java.time.Instant;
import java.util.UUID;

public record ConnectionBroken(UUID connectionId, String reason, Instant occurredAt) {}
