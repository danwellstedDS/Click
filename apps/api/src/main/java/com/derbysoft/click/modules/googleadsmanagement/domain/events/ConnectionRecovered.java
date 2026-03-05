package com.derbysoft.click.modules.googleadsmanagement.domain.events;

import java.time.Instant;
import java.util.UUID;

public record ConnectionRecovered(UUID connectionId, Instant occurredAt) {}
