package com.derbysoft.click.modules.ingestion.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "raw_campaign_rows")
public class RawCampaignRowEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "snapshot_id")
    private UUID snapshotId;

    @Column(name = "integration_id", nullable = false, updatable = false)
    private UUID integrationId;

    @Column(name = "account_id", nullable = false, length = 20, updatable = false)
    private String accountId;

    @Column(name = "campaign_id", nullable = false, length = 30, updatable = false)
    private String campaignId;

    @Column(name = "campaign_name", length = 255)
    private String campaignName;

    @Column(name = "report_date", nullable = false, updatable = false)
    private LocalDate reportDate;

    @Column(nullable = false)
    private long clicks;

    @Column(nullable = false)
    private long impressions;

    @Column(name = "cost_micros", nullable = false)
    private long costMicros;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal conversions;

    @Column(name = "ingested_at", nullable = false, updatable = false)
    private Instant ingestedAt;

    protected RawCampaignRowEntity() {}

    public RawCampaignRowEntity(UUID id, UUID snapshotId, UUID integrationId, String accountId,
                                 String campaignId, String campaignName, LocalDate reportDate,
                                 long clicks, long impressions, long costMicros,
                                 double conversions, Instant ingestedAt) {
        this.id = id;
        this.snapshotId = snapshotId;
        this.integrationId = integrationId;
        this.accountId = accountId;
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.reportDate = reportDate;
        this.clicks = clicks;
        this.impressions = impressions;
        this.costMicros = costMicros;
        this.conversions = BigDecimal.valueOf(conversions);
        this.ingestedAt = ingestedAt;
    }

    public UUID getId() { return id; }
    public UUID getSnapshotId() { return snapshotId; }
    public UUID getIntegrationId() { return integrationId; }
    public String getAccountId() { return accountId; }
    public String getCampaignId() { return campaignId; }
    public String getCampaignName() { return campaignName; }
    public LocalDate getReportDate() { return reportDate; }
    public long getClicks() { return clicks; }
    public long getImpressions() { return impressions; }
    public long getCostMicros() { return costMicros; }
    public BigDecimal getConversions() { return conversions; }
    public Instant getIngestedAt() { return ingestedAt; }

    public void setSnapshotId(UUID snapshotId) { this.snapshotId = snapshotId; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }
    public void setClicks(long clicks) { this.clicks = clicks; }
    public void setImpressions(long impressions) { this.impressions = impressions; }
    public void setCostMicros(long costMicros) { this.costMicros = costMicros; }
    public void setConversions(BigDecimal conversions) { this.conversions = conversions; }
}
