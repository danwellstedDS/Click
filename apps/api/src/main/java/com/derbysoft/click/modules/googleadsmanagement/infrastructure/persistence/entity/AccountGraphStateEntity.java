package com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_graph_state")
public class AccountGraphStateEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "connection_id", nullable = false, updatable = false)
    private UUID connectionId;

    @Column(name = "customer_id", nullable = false, length = 20)
    private String customerId;

    @Column(name = "account_name", length = 255)
    private String accountName;

    @Column(name = "currency_code", length = 10)
    private String currencyCode;

    @Column(name = "time_zone", length = 100)
    private String timeZone;

    @Column(name = "discovered_at", nullable = false)
    private Instant discoveredAt;

    protected AccountGraphStateEntity() {}

    public AccountGraphStateEntity(
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
