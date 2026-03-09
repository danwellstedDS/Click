package com.derbysoft.click.modules.campaignexecution.interfaces.http.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.derbysoft.click.modules.campaignexecution.application.handlers.ForceRunPlanItemHandler;
import com.derbysoft.click.modules.campaignexecution.application.handlers.RetryPlanItemHandler;
import com.derbysoft.click.modules.campaignexecution.domain.PlanItemRepository;
import com.derbysoft.click.modules.campaignexecution.domain.WriteActionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.entities.PlanItem;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.ApplyOrder;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.PlanItemStatus;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionType;
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

@WebMvcTest(PlanItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlanItemControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private PlanItemRepository planItemRepository;
    @MockitoBean private WriteActionRepository writeActionRepository;
    @MockitoBean private ForceRunPlanItemHandler forceRunHandler;
    @MockitoBean private RetryPlanItemHandler retryHandler;
    @MockitoBean private JwtService jwtService;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID PLAN_ID = UUID.randomUUID();
    private static final UUID REV_ID = UUID.randomUUID();
    private static final UUID ITEM_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-03-06T09:00:00Z");

    private PlanItem aQueuedItem() {
        PlanItem item = PlanItem.create(ITEM_ID, REV_ID, UUID.randomUUID(),
            WriteActionType.CREATE_CAMPAIGN, "CAMPAIGN", null,
            "{}", ApplyOrder.CAMPAIGN, NOW);
        item.publish();
        item.enqueue(NOW);
        item.clearEvents();
        return item;
    }

    @Test
    void shouldReturn200WithItemsList() throws Exception {
        when(planItemRepository.findByRevisionId(REV_ID)).thenReturn(List.of(aQueuedItem()));

        mockMvc.perform(get("/api/v1/campaign-plans/{planId}/revisions/{revId}/items",
                PLAN_ID, REV_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].status").value("QUEUED"));
    }

    @Test
    void shouldReturn202OnRetryItem() throws Exception {
        PlanItem item = aQueuedItem();
        when(retryHandler.retry(eq(ITEM_ID), any(), any(), any())).thenReturn(item);

        mockMvc.perform(post("/api/v1/campaign-plans/{planId}/revisions/{revId}/items/{itemId}/retry",
                PLAN_ID, REV_ID, ITEM_ID)
                .header("X-Tenant-Id", TENANT_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"reason": "transient error resolved"}
                    """))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldReturn202OnForceRunItem() throws Exception {
        PlanItem item = aQueuedItem();
        when(forceRunHandler.forceRun(eq(ITEM_ID), any(), any(), any())).thenReturn(item);

        mockMvc.perform(post("/api/v1/campaign-plans/{planId}/revisions/{revId}/items/{itemId}/force-run",
                PLAN_ID, REV_ID, ITEM_ID)
                .header("X-Tenant-Id", TENANT_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"reason": "ops override"}
                    """))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.success").value(true));
    }
}
