package com.derbysoft.click.modules.normalisation.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "canonical_facts")
public class CanonicalFactEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "canonical_batch_id", nullable = false, updatable = false)
    private UUID canonicalBatchId;

    @Column(name = "source_snapshot_id", nullable = false, updatable = false)
    private UUID sourceSnapshotId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 30)
    private String channel;

    @Column(name = "integration_id", nullable = false, updatable = false)
    private UUID integrationId;

    @Column(name = "customer_account_id", nullable = false, length = 20, updatable = false)
    private String customerAccountId;

    @Column(name = "campaign_id", nullable = false, length = 30, updatable = false)
    private String campaignId;

    @Column(name = "campaign_name", length = 255)
    private String campaignName;

    @Column(name = "report_date", nullable = false, updatable = false)
    private LocalDate reportDate;

    @Column(nullable = false)
    private long impressions;

    @Column(nullable = false)
    private long clicks;

    @Column(name = "cost_micros", nullable = false)
    private long costMicros;

    @Column(name = "cost_amount", nullable = false, precision = 18, scale = 6,
            insertable = false, updatable = false)
    private BigDecimal costAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal conversions;

    @Column(name = "mapping_version", nullable = false, length = 20, updatable = false)
    private String mappingVersion;

    @Column(name = "reconciliation_key", nullable = false, length = 64, updatable = false)
    private String reconciliationKey;

    @Column(name = "quality_flags", columnDefinition = "text[]", nullable = false)
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Array(length = 10)
    private String[] qualityFlags;

    @Column(nullable = false)
    private boolean quarantined;

    @Column(name = "ingested_at", nullable = false, updatable = false)
    private Instant ingestedAt;

    public CanonicalFactEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCanonicalBatchId() { return canonicalBatchId; }
    public void setCanonicalBatchId(UUID canonicalBatchId) { this.canonicalBatchId = canonicalBatchId; }
    public UUID getSourceSnapshotId() { return sourceSnapshotId; }
    public void setSourceSnapshotId(UUID sourceSnapshotId) { this.sourceSnapshotId = sourceSnapshotId; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public UUID getIntegrationId() { return integrationId; }
    public void setIntegrationId(UUID integrationId) { this.integrationId = integrationId; }
    public String getCustomerAccountId() { return customerAccountId; }
    public void setCustomerAccountId(String customerAccountId) { this.customerAccountId = customerAccountId; }
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }
    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
    public long getImpressions() { return impressions; }
    public void setImpressions(long impressions) { this.impressions = impressions; }
    public long getClicks() { return clicks; }
    public void setClicks(long clicks) { this.clicks = clicks; }
    public long getCostMicros() { return costMicros; }
    public void setCostMicros(long costMicros) { this.costMicros = costMicros; }
    public BigDecimal getCostAmount() { return costAmount; }
    public BigDecimal getConversions() { return conversions; }
    public void setConversions(BigDecimal conversions) { this.conversions = conversions; }
    public String getMappingVersion() { return mappingVersion; }
    public void setMappingVersion(String mappingVersion) { this.mappingVersion = mappingVersion; }
    public String getReconciliationKey() { return reconciliationKey; }
    public void setReconciliationKey(String reconciliationKey) { this.reconciliationKey = reconciliationKey; }
    public String[] getQualityFlags() { return qualityFlags; }
    public void setQualityFlags(String[] qualityFlags) { this.qualityFlags = qualityFlags; }
    public boolean isQuarantined() { return quarantined; }
    public void setQuarantined(boolean quarantined) { this.quarantined = quarantined; }
    public Instant getIngestedAt() { return ingestedAt; }
    public void setIngestedAt(Instant ingestedAt) { this.ingestedAt = ingestedAt; }
}
