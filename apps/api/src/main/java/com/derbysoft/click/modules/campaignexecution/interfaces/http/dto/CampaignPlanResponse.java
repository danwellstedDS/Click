package com.derbysoft.click.modules.campaignexecution.interfaces.http.dto;

import java.time.Instant;
import java.util.UUID;

public record CampaignPlanResponse(UUID id, UUID tenantId, String name, String description,
                                    Instant createdAt, Instant updatedAt) {}
