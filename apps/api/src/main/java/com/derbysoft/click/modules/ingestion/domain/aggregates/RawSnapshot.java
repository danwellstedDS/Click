package com.derbysoft.click.modules.ingestion.domain.aggregates;

import com.derbysoft.click.modules.ingestion.domain.events.RawSnapshotWritten;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.DateWindow;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class RawSnapshot {

    private final UUID id;
    private final UUID syncJobId;
    private final UUID integrationId;
    private final UUID tenantId;
    private final String accountId;
    private final String reportType;
    private final DateWindow dateWindow;
    private final int rowCount;
    private final String checksum;
    private final Instant createdAt;
    private final List<Object> events = new ArrayList<>();

    private RawSnapshot(
        UUID id, UUID syncJobId, UUID integrationId, UUID tenantId,
        String accountId, String reportType, DateWindow dateWindow,
        int rowCount, String checksum, Instant createdAt
    ) {
        this.id = id;
        this.syncJobId = syncJobId;
        this.integrationId = integrationId;
        this.tenantId = tenantId;
        this.accountId = accountId;
        this.reportType = reportType;
        this.dateWindow = dateWindow;
        this.rowCount = rowCount;
        this.checksum = checksum;
        this.createdAt = createdAt;
    }

    public static RawSnapshot create(
        UUID id, UUID syncJobId, UUID integrationId, UUID tenantId,
        String accountId, String reportType, DateWindow dateWindow,
        int rowCount, String checksum, Instant now
    ) {
        RawSnapshot snapshot = new RawSnapshot(
            id, syncJobId, integrationId, tenantId,
            accountId, reportType, dateWindow,
            rowCount, checksum, now
        );
        snapshot.events.add(new RawSnapshotWritten(
            id, syncJobId, integrationId, tenantId,
            accountId, reportType,
            dateWindow.from(), dateWindow.to(),
            rowCount, now
        ));
        return snapshot;
    }

    public static RawSnapshot reconstitute(
        UUID id, UUID syncJobId, UUID integrationId, UUID tenantId,
        String accountId, String reportType, DateWindow dateWindow,
        int rowCount, String checksum, Instant createdAt
    ) {
        return new RawSnapshot(
            id, syncJobId, integrationId, tenantId,
            accountId, reportType, dateWindow,
            rowCount, checksum, createdAt
        );
    }

    public UUID getId() { return id; }
    public UUID getSyncJobId() { return syncJobId; }
    public UUID getIntegrationId() { return integrationId; }
    public UUID getTenantId() { return tenantId; }
    public String getAccountId() { return accountId; }
    public String getReportType() { return reportType; }
    public DateWindow getDateWindow() { return dateWindow; }
    public int getRowCount() { return rowCount; }
    public String getChecksum() { return checksum; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Object> getEvents() { return Collections.unmodifiableList(events); }
    public void clearEvents() { events.clear(); }
}
