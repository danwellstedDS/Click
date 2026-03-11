package com.derbysoft.click.modules.googleadsmanagement.api.events;

import java.util.UUID;

public record AccessFailureObserved(
    UUID tenantId,
    String customerId,
    String reason
) {}
