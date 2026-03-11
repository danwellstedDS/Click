package com.derbysoft.click.modules.normalisation.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.derbysoft.click.modules.normalisation.application.ports.RawCampaignRowQueryPort.RawCampaignRowData;
import com.derbysoft.click.modules.normalisation.application.services.QualityValidator;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.QualityFlag;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QualityValidatorTest {

    private QualityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new QualityValidator();
    }

    private RawCampaignRowData row(long impressions, long clicks, long costMicros,
                                   BigDecimal conversions, String campaignId) {
        return new RawCampaignRowData(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            "acc-1", campaignId, "Test Campaign",
            LocalDate.now(), clicks, impressions, costMicros, conversions
        );
    }

    private RawCampaignRowData cleanRow() {
        return row(100, 10, 500000L, new BigDecimal("2.0"), "camp-1");
    }

    @Test
    void shouldReturnNoFlagsForCleanRow() {
        List<QualityFlag> flags = validator.validate(cleanRow());

        assertThat(flags).isEmpty();
    }

    @Test
    void shouldFlagNegativeImpressions() {
        RawCampaignRowData r = row(-1, 10, 500000L, new BigDecimal("2.0"), "camp-1");

        assertThat(validator.validate(r)).containsExactly(QualityFlag.NEGATIVE_IMPRESSIONS);
    }

    @Test
    void shouldFlagNegativeClicks() {
        RawCampaignRowData r = row(100, -1, 500000L, new BigDecimal("2.0"), "camp-1");

        assertThat(validator.validate(r)).containsExactly(QualityFlag.NEGATIVE_CLICKS);
    }

    @Test
    void shouldFlagNegativeCost() {
        RawCampaignRowData r = row(100, 10, -1L, new BigDecimal("2.0"), "camp-1");

        assertThat(validator.validate(r)).containsExactly(QualityFlag.NEGATIVE_COST);
    }

    @Test
    void shouldFlagNegativeConversions() {
        RawCampaignRowData r = row(100, 10, 500000L, new BigDecimal("-0.1"), "camp-1");

        assertThat(validator.validate(r)).containsExactly(QualityFlag.NEGATIVE_CONVERSIONS);
    }

    @Test
    void shouldNotFlagNullConversions() {
        RawCampaignRowData r = row(100, 10, 500000L, null, "camp-1");

        assertThat(validator.validate(r)).isEmpty();
    }

    @Test
    void shouldFlagNullCampaignId() {
        RawCampaignRowData r = row(100, 10, 500000L, new BigDecimal("2.0"), null);

        assertThat(validator.validate(r)).containsExactly(QualityFlag.MISSING_CAMPAIGN_ID);
    }

    @Test
    void shouldFlagBlankCampaignId() {
        RawCampaignRowData r = row(100, 10, 500000L, new BigDecimal("2.0"), "  ");

        assertThat(validator.validate(r)).containsExactly(QualityFlag.MISSING_CAMPAIGN_ID);
    }

    @Test
    void shouldAccumulateMultipleFlags() {
        RawCampaignRowData r = row(-1, 10, -1L, new BigDecimal("2.0"), "camp-1");

        List<QualityFlag> flags = validator.validate(r);
        assertThat(flags).hasSize(2);
        assertThat(flags).contains(QualityFlag.NEGATIVE_IMPRESSIONS, QualityFlag.NEGATIVE_COST);
    }
}
