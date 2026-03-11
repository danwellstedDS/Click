package com.derbysoft.click.modules.normalisation.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.derbysoft.click.modules.normalisation.application.ports.RawCampaignRowQueryPort.RawCampaignRowData;
import com.derbysoft.click.modules.normalisation.application.services.Normalizer;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.MappingVersion;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.ReconciliationKey;
import com.derbysoft.click.modules.normalisation.infrastructure.persistence.entity.CanonicalFactEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NormalizerTest {

    private Normalizer normalizer;

    private static final UUID BATCH_ID = UUID.randomUUID();
    private static final UUID INTEGRATION_ID = UUID.randomUUID();
    private static final UUID SNAPSHOT_ID = UUID.randomUUID();
    private static final String ACCOUNT_ID = "acc-1";
    private static final String CAMPAIGN_ID = "camp-1";
    private static final LocalDate REPORT_DATE = LocalDate.of(2026, 3, 1);
    private static final Instant NOW = Instant.now();

    @BeforeEach
    void setUp() {
        normalizer = new Normalizer();
    }

    private RawCampaignRowData rawRow() {
        return new RawCampaignRowData(
            UUID.randomUUID(), SNAPSHOT_ID, INTEGRATION_ID,
            ACCOUNT_ID, CAMPAIGN_ID, "Test Campaign",
            REPORT_DATE, 10L, 100L, 500000L, new BigDecimal("2.0")
        );
    }

    @Test
    void shouldMapAllCoreFieldsFromRawRow() {
        RawCampaignRowData row = rawRow();
        CanonicalFactEntity entity = normalizer.map(row, BATCH_ID, MappingVersion.V1, NOW);

        assertThat(entity.getChannel()).isEqualTo("GOOGLE_ADS");
        assertThat(entity.getCampaignId()).isEqualTo(CAMPAIGN_ID);
        assertThat(entity.getReportDate()).isEqualTo(REPORT_DATE);
        assertThat(entity.getImpressions()).isEqualTo(100L);
        assertThat(entity.getClicks()).isEqualTo(10L);
        assertThat(entity.getCostMicros()).isEqualTo(500000L);
        assertThat(entity.getConversions()).isEqualByComparingTo(new BigDecimal("2.0"));
    }

    @Test
    void shouldAssignReconciliationKey() {
        RawCampaignRowData row = rawRow();
        CanonicalFactEntity entity = normalizer.map(row, BATCH_ID, MappingVersion.V1, NOW);

        String expectedKey = ReconciliationKey.from(INTEGRATION_ID, ACCOUNT_ID, CAMPAIGN_ID, REPORT_DATE).value();
        assertThat(entity.getReconciliationKey()).isNotNull();
        assertThat(entity.getReconciliationKey()).hasSize(64);
        assertThat(entity.getReconciliationKey()).isEqualTo(expectedKey);
    }

    @Test
    void shouldInitialiseQualityFlagsAsEmpty() {
        CanonicalFactEntity entity = normalizer.map(rawRow(), BATCH_ID, MappingVersion.V1, NOW);

        assertThat(entity.getQualityFlags()).isEmpty();
    }

    @Test
    void shouldInitialiseQuarantinedFalse() {
        CanonicalFactEntity entity = normalizer.map(rawRow(), BATCH_ID, MappingVersion.V1, NOW);

        assertThat(entity.isQuarantined()).isFalse();
    }

    @Test
    void shouldSetIngestedAtFromNowArg() {
        CanonicalFactEntity entity = normalizer.map(rawRow(), BATCH_ID, MappingVersion.V1, NOW);

        assertThat(entity.getIngestedAt()).isEqualTo(NOW);
    }
}
