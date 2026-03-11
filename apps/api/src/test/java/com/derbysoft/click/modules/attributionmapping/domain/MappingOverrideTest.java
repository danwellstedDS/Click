package com.derbysoft.click.modules.attributionmapping.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.derbysoft.click.modules.attributionmapping.domain.aggregates.MappingOverride;
import com.derbysoft.click.modules.attributionmapping.domain.events.MappingOverrideRemoved;
import com.derbysoft.click.modules.attributionmapping.domain.events.MappingOverrideSet;
import com.derbysoft.click.modules.attributionmapping.domain.valueobjects.OverrideScope;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MappingOverrideTest {

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID TARGET_NODE_ID = UUID.randomUUID();

    @Test
    void shouldSetAccountLevelOverride() {
        MappingOverride override = MappingOverride.set(
            TENANT_ID, OverrideScope.ACCOUNT, "123", null,
            TARGET_NODE_ID, "Property", "manual fix", "user1", Instant.now());

        assertThat(override.getStatus()).isEqualTo("ACTIVE");
        assertThat(override.getScopeType()).isEqualTo(OverrideScope.ACCOUNT);
        assertThat(override.getCampaignId()).isNull();
        assertThat(override.getTargetOrgNodeId()).isEqualTo(TARGET_NODE_ID);
        assertThat(override.getEvents()).hasSize(1);
        assertThat(override.getEvents().get(0)).isInstanceOf(MappingOverrideSet.class);
    }

    @Test
    void shouldSetCampaignLevelOverride() {
        MappingOverride override = MappingOverride.set(
            TENANT_ID, OverrideScope.ACCOUNT_CAMPAIGN, "123", "campaign-456",
            TARGET_NODE_ID, "Property", "campaign fix", "user1", Instant.now());

        assertThat(override.getScopeType()).isEqualTo(OverrideScope.ACCOUNT_CAMPAIGN);
        assertThat(override.getCampaignId()).isEqualTo("campaign-456");
    }

    @Test
    void shouldRemoveActiveOverride() {
        MappingOverride override = MappingOverride.set(
            TENANT_ID, OverrideScope.ACCOUNT, "123", null,
            TARGET_NODE_ID, "Property", "reason", "user1", Instant.now());
        override.clearEvents();

        override.remove("no longer needed", "user2", Instant.now());

        assertThat(override.getStatus()).isEqualTo("REMOVED");
        assertThat(override.getRemovedReason()).isEqualTo("no longer needed");
        assertThat(override.getEvents()).hasSize(1);
        assertThat(override.getEvents().get(0)).isInstanceOf(MappingOverrideRemoved.class);
    }

    @Test
    void shouldRejectDoubleRemoval() {
        MappingOverride override = MappingOverride.set(
            TENANT_ID, OverrideScope.ACCOUNT, "123", null,
            TARGET_NODE_ID, "Property", "reason", "user1", Instant.now());
        override.remove("first removal", "user2", Instant.now());

        assertThatThrownBy(() -> override.remove("second removal", "user3", Instant.now()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("REMOVED");
    }
}
