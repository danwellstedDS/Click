package com.derbysoft.click.modules.normalisation.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReconciliationKeyTest {

    private static final UUID INTEGRATION_ID = UUID.randomUUID();
    private static final String ACCOUNT_ID = "acc-1";
    private static final String CAMPAIGN_ID = "camp-1";
    private static final LocalDate DATE = LocalDate.of(2026, 3, 1);

    @Test
    void shouldProduceDeterministicKey() {
        ReconciliationKey key1 = ReconciliationKey.from(INTEGRATION_ID, ACCOUNT_ID, CAMPAIGN_ID, DATE);
        ReconciliationKey key2 = ReconciliationKey.from(INTEGRATION_ID, ACCOUNT_ID, CAMPAIGN_ID, DATE);

        assertThat(key1.value()).isEqualTo(key2.value());
    }

    @Test
    void shouldProduceSixtyFourCharHexString() {
        ReconciliationKey key = ReconciliationKey.from(INTEGRATION_ID, ACCOUNT_ID, CAMPAIGN_ID, DATE);

        assertThat(key.value()).hasSize(64);
        assertThat(key.value()).matches("[0-9a-f]+");
    }

    @Test
    void shouldProduceDifferentKeysForDifferentCampaignIds() {
        ReconciliationKey keyA = ReconciliationKey.from(INTEGRATION_ID, ACCOUNT_ID, "cA", DATE);
        ReconciliationKey keyB = ReconciliationKey.from(INTEGRATION_ID, ACCOUNT_ID, "cB", DATE);

        assertThat(keyA.value()).isNotEqualTo(keyB.value());
    }

    @Test
    void shouldProduceDifferentKeysForDifferentDates() {
        LocalDate date1 = LocalDate.of(2026, 3, 1);
        LocalDate date2 = LocalDate.of(2026, 3, 2);

        ReconciliationKey key1 = ReconciliationKey.from(INTEGRATION_ID, ACCOUNT_ID, CAMPAIGN_ID, date1);
        ReconciliationKey key2 = ReconciliationKey.from(INTEGRATION_ID, ACCOUNT_ID, CAMPAIGN_ID, date2);

        assertThat(key1.value()).isNotEqualTo(key2.value());
    }
}
