package com.derbysoft.click.modules.ingestion.api.contracts;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SyncJobInfo(
    UUID id,
    UUID integrationId,
    String accountId,
    String reportType,
    LocalDate dateFrom,
    LocalDate dateTo,
    String status,
    int attempts,
    String triggerType,
    String failureClass,
    Instant createdAt
) {}
