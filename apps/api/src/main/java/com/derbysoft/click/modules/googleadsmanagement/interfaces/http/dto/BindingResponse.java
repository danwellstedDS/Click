package com.derbysoft.click.modules.googleadsmanagement.interfaces.http.dto;

import java.time.Instant;
import java.util.UUID;

public record BindingResponse(
    UUID id,
    UUID connectionId,
    UUID tenantId,
    String customerId,
    String status,
    String bindingType,
    Instant createdAt
) {}
