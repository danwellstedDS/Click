package com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository;

import com.derbysoft.click.modules.ingestion.infrastructure.persistence.entity.RawCampaignRowEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RawCampaignRowJpaRepository extends JpaRepository<RawCampaignRowEntity, UUID> {

    List<RawCampaignRowEntity> findBySnapshotId(UUID snapshotId);

    @Modifying
    @Query(value = """
        INSERT INTO raw_campaign_rows
            (id, snapshot_id, integration_id, account_id, campaign_id, campaign_name,
             report_date, clicks, impressions, cost_micros, conversions, ingested_at)
        VALUES
            (:#{#row.id}, :#{#row.snapshotId}, :#{#row.integrationId}, :#{#row.accountId},
             :#{#row.campaignId}, :#{#row.campaignName}, :#{#row.reportDate},
             :#{#row.clicks}, :#{#row.impressions}, :#{#row.costMicros},
             :#{#row.conversions}, :#{#row.ingestedAt})
        ON CONFLICT (integration_id, account_id, campaign_id, report_date)
        DO UPDATE SET
            snapshot_id = EXCLUDED.snapshot_id,
            campaign_name = EXCLUDED.campaign_name,
            clicks = EXCLUDED.clicks,
            impressions = EXCLUDED.impressions,
            cost_micros = EXCLUDED.cost_micros,
            conversions = EXCLUDED.conversions,
            ingested_at = EXCLUDED.ingested_at
        """, nativeQuery = true)
    void upsert(@Param("row") RawCampaignRowEntity row);

    default void upsertAll(List<RawCampaignRowEntity> rows) {
        rows.forEach(this::upsert);
    }

    @Modifying
    @Query(value = """
        UPDATE raw_campaign_rows
        SET snapshot_id = :snapshotId
        WHERE integration_id = :integrationId
          AND account_id = :accountId
          AND report_date BETWEEN :dateFrom AND :dateTo
        """, nativeQuery = true)
    void updateSnapshotIdForJob(
        @Param("snapshotId") UUID snapshotId,
        @Param("integrationId") UUID integrationId,
        @Param("accountId") String accountId,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo);
}
