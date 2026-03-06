package com.derbysoft.click.modules.campaignexecution.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.derbysoft.click.modules.campaignexecution.domain.aggregates.CampaignPlan;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CampaignPlanTest {

    private static final UUID ID = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-03-06T09:00:00Z");

    private CampaignPlan newPlan() {
        return CampaignPlan.create(ID, TENANT_ID, "Test Plan", "A plan for testing", NOW);
    }

    @Test
    void shouldCreatePlanWithGivenFields() {
        CampaignPlan plan = newPlan();

        assertThat(plan.getId()).isEqualTo(ID);
        assertThat(plan.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(plan.getName()).isEqualTo("Test Plan");
        assertThat(plan.getDescription()).isEqualTo("A plan for testing");
        assertThat(plan.getCreatedAt()).isEqualTo(NOW);
        assertThat(plan.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldRenamePlan() {
        CampaignPlan plan = newPlan();
        Instant later = NOW.plusSeconds(60);

        plan.rename("Updated Plan", later);

        assertThat(plan.getName()).isEqualTo("Updated Plan");
        assertThat(plan.getUpdatedAt()).isEqualTo(later);
    }

    @Test
    void shouldHaveNoEventsOnCreate() {
        CampaignPlan plan = newPlan();
        assertThat(plan.getEvents()).isEmpty();
    }
}
