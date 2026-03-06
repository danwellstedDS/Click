package com.derbysoft.click.modules.campaignexecution.api.contracts;

import java.time.Instant;
import java.util.UUID;

public record PlanItemInfo(UUID itemId, UUID revisionId, String actionType,
                            String resourceType, String resourceId, String status,
                            int attempts, Instant updatedAt) {}
