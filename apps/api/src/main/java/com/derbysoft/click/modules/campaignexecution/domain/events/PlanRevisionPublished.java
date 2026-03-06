package com.derbysoft.click.modules.campaignexecution.domain.events;

import java.time.Instant;
import java.util.UUID;

public record PlanRevisionPublished(UUID revisionId, UUID planId, UUID tenantId,
                                     String publishedBy, Instant occurredAt) {}
