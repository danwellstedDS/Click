package com.derbysoft.click.modules.campaignexecution.domain.events;

import java.time.Instant;
import java.util.UUID;

public record PlanRevisionCancelled(UUID revisionId, UUID planId, UUID tenantId,
                                     String cancelledBy, String reason, Instant occurredAt) {}
