package com.derbysoft.click.modules.campaignexecution.domain.events;

import java.util.UUID;

public record AccessFailureObserved(UUID tenantId, String customerId, String reason) {}
