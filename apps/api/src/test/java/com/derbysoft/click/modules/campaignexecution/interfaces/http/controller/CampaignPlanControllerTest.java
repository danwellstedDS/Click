package com.derbysoft.click.modules.campaignexecution.interfaces.http.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.derbysoft.click.modules.campaignexecution.application.handlers.CampaignPlanService;
import com.derbysoft.click.modules.campaignexecution.application.handlers.PlanApplyService;
import com.derbysoft.click.modules.campaignexecution.domain.CampaignPlanRepository;
import com.derbysoft.click.modules.campaignexecution.domain.PlanRevisionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.CampaignPlan;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.PlanRevision;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.JwtService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CampaignPlanController.class)
@AutoConfigureMockMvc(addFilters = false)
class CampaignPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private CampaignPlanService campaignPlanService;
    @MockitoBean private PlanApplyService planApplyService;
    @MockitoBean private CampaignPlanRepository campaignPlanRepository;
    @MockitoBean private PlanRevisionRepository planRevisionRepository;
    @MockitoBean private JwtService jwtService;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID PLAN_ID = UUID.randomUUID();
    private static final UUID REVISION_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-03-06T09:00:00Z");

    private CampaignPlan aPlan() {
        return CampaignPlan.reconstitute(PLAN_ID, TENANT_ID, "Test Plan", "desc", NOW, NOW);
    }

    private PlanRevision aPublishedRevision() {
        PlanRevision r = PlanRevision.create(REVISION_ID, PLAN_ID, TENANT_ID, 1, NOW);
        r.publish("user-1", NOW);
        r.clearEvents();
        return r;
    }

    @Test
    void shouldReturn201OnCreatePlan() throws Exception {
        when(campaignPlanService.createPlan(any(), eq("Test Plan"), any(), any()))
            .thenReturn(aPlan());

        mockMvc.perform(post("/api/v1/campaign-plans")
                .header("X-Tenant-Id", TENANT_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "Test Plan", "description": "desc"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("Test Plan"));
    }

    @Test
    void shouldReturn200OnListPlans() throws Exception {
        when(campaignPlanRepository.findByTenantId(TENANT_ID)).thenReturn(List.of(aPlan()));

        mockMvc.perform(get("/api/v1/campaign-plans")
                .param("tenantId", TENANT_ID.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("Test Plan"));
    }

    @Test
    void shouldReturn200OnPublishRevision() throws Exception {
        when(campaignPlanService.publishRevision(eq(REVISION_ID), any(), any()))
            .thenReturn(aPublishedRevision());

        mockMvc.perform(post("/api/v1/campaign-plans/{planId}/revisions/{revId}/publish",
                PLAN_ID, REVISION_ID)
                .header("X-Tenant-Id", TENANT_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    @Test
    void shouldReturn202OnApplyRevision() throws Exception {
        PlanRevision applying = PlanRevision.create(REVISION_ID, PLAN_ID, TENANT_ID, 1, NOW);
        applying.publish("user-1", NOW);
        applying.startApply(NOW);
        applying.clearEvents();

        when(planApplyService.applyRevision(eq(REVISION_ID), any(), any(), any()))
            .thenReturn(applying);

        mockMvc.perform(post("/api/v1/campaign-plans/{planId}/revisions/{revId}/apply",
                PLAN_ID, REVISION_ID)
                .header("X-Tenant-Id", TENANT_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"manual apply\"}"))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.data.status").value("APPLYING"));
    }

    @Test
    void shouldReturn200OnCancelRevision() throws Exception {
        PlanRevision cancelled = PlanRevision.create(REVISION_ID, PLAN_ID, TENANT_ID, 1, NOW);
        cancelled.cancel("user-1", "no longer needed", NOW);
        cancelled.clearEvents();

        when(campaignPlanService.cancelRevision(eq(REVISION_ID), any(), any()))
            .thenReturn(cancelled);

        mockMvc.perform(post("/api/v1/campaign-plans/{planId}/revisions/{revId}/cancel",
                PLAN_ID, REVISION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"reason": "no longer needed"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }
}
