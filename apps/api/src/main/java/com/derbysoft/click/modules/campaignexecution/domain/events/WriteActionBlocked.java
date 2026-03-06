package com.derbysoft.click.modules.campaignexecution.domain.events;

import java.time.Instant;
import java.util.UUID;

public record WriteActionBlocked(UUID itemId, UUID revisionId, UUID tenantId,
                                  String reason, Instant occurredAt) {}
