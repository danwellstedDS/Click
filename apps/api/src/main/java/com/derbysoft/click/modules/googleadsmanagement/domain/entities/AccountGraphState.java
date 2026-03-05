package com.derbysoft.click.modules.googleadsmanagement.domain.entities;

import java.time.Instant;
import java.util.UUID;

public final class AccountGraphState {

    private final UUID id;
    private final UUID connectionId;
    private final String customerId;
    private final String accountName;
    private final String currencyCode;
    private final String timeZone;
    private final Instant discoveredAt;

    public AccountGraphState(
        UUID id,
        UUID connectionId,
        String customerId,
        String accountName,
        String currencyCode,
        String timeZone,
        Instant discoveredAt
    ) {
        this.id = id;
        this.connectionId = connectionId;
        this.customerId = customerId;
        this.accountName = accountName;
        this.currencyCode = currencyCode;
        this.timeZone = timeZone;
        this.discoveredAt = discoveredAt;
    }

    public UUID getId() { return id; }
    public UUID getConnectionId() { return connectionId; }
    public String getCustomerId() { return customerId; }
    public String getAccountName() { return accountName; }
    public String getCurrencyCode() { return currencyCode; }
    public String getTimeZone() { return timeZone; }
    public Instant getDiscoveredAt() { return discoveredAt; }
}
