package com.derbysoft.click.modules.ingestion.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "raw_snapshots")
public class RawSnapshotEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "sync_job_id", nullable = false, updatable = false)
    private UUID syncJobId;

    @Column(name = "integration_id", nullable = false, updatable = false)
    private UUID integrationId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "account_id", nullable = false, length = 20, updatable = false)
    private String accountId;

    @Column(name = "report_type", nullable = false, length = 50, updatable = false)
    private String reportType;

    @Column(name = "date_from", nullable = false, updatable = false)
    private LocalDate dateFrom;

    @Column(name = "date_to", nullable = false, updatable = false)
    private LocalDate dateTo;

    @Column(name = "row_count", nullable = false, updatable = false)
    private int rowCount;

    @Column(nullable = false, length = 64, updatable = false)
    private String checksum;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RawSnapshotEntity() {}

    public RawSnapshotEntity(UUID id, UUID syncJobId, UUID integrationId, UUID tenantId,
                              String accountId, String reportType,
                              LocalDate dateFrom, LocalDate dateTo,
                              int rowCount, String checksum) {
        this.id = id;
        this.syncJobId = syncJobId;
        this.integrationId = integrationId;
        this.tenantId = tenantId;
        this.accountId = accountId;
        this.reportType = reportType;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.rowCount = rowCount;
        this.checksum = checksum;
    }

    public UUID getId() { return id; }
    public UUID getSyncJobId() { return syncJobId; }
    public UUID getIntegrationId() { return integrationId; }
    public UUID getTenantId() { return tenantId; }
    public String getAccountId() { return accountId; }
    public String getReportType() { return reportType; }
    public LocalDate getDateFrom() { return dateFrom; }
    public LocalDate getDateTo() { return dateTo; }
    public int getRowCount() { return rowCount; }
    public String getChecksum() { return checksum; }
    public Instant getCreatedAt() { return createdAt; }
}
