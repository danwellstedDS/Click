package com.derbysoft.click.modules.ingestion.domain.valueobjects;

import java.util.UUID;

public record IdempotencyKey(UUID integrationId, String accountId, DateWindow dateWindow, String reportType) {

    public String toKey() {
        return integrationId + ":" + accountId + ":" + dateWindow.from() + ":" + dateWindow.to() + ":" + reportType;
    }
}
