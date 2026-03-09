package com.derbysoft.click.modules.campaignexecution.domain.events;

import java.time.Instant;
import java.util.UUID;

public record PlanRevisionApplyStarted(UUID revisionId, UUID planId, UUID tenantId,
                                        Instant occurredAt) {}
