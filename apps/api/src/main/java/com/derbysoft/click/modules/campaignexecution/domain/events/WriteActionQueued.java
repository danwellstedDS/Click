package com.derbysoft.click.modules.campaignexecution.domain.events;

import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionType;
import java.time.Instant;
import java.util.UUID;

public record WriteActionQueued(UUID itemId, UUID revisionId, UUID tenantId,
                                 WriteActionType actionType, Instant occurredAt) {}
