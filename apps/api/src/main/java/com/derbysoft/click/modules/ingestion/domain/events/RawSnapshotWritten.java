package com.derbysoft.click.modules.ingestion.domain.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RawSnapshotWritten(
    UUID snapshotId,
    UUID jobId,
    UUID integrationId,
    UUID tenantId,
    String accountId,
    String reportType,
    LocalDate dateFrom,
    LocalDate dateTo,
    int rowCount,
    Instant occurredAt
) {}
