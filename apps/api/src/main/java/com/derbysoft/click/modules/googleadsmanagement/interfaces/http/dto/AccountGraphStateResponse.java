package com.derbysoft.click.modules.googleadsmanagement.interfaces.http.dto;

import java.time.Instant;

public record AccountGraphStateResponse(
    String customerId,
    String accountName,
    String currencyCode,
    String timeZone,
    Instant discoveredAt
) {}
