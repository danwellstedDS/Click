package com.derbysoft.click.modules.googleadsmanagement.domain.events;

import java.time.Instant;
import java.util.UUID;

public record BindingStaleFlagged(UUID bindingId, Instant occurredAt) {}
