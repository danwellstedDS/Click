package com.derbysoft.click.modules.ingestion.interfaces.http.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.derbysoft.click.modules.identityaccess.infrastructure.security.JwtService;
import com.derbysoft.click.modules.ingestion.api.contracts.SyncJobInfo;
import com.derbysoft.click.modules.ingestion.api.ports.IngestionQueryPort;
import com.derbysoft.click.modules.ingestion.application.handlers.IngestionJobService;
import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncJob;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.DateWindow;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.TriggerType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SyncJobController.class)
@AutoConfigureMockMvc(addFilters = false)
class SyncJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngestionJobService ingestionJobService;

    @MockitoBean
    private IngestionQueryPort ingestionQueryPort;

    @MockitoBean
    private JwtService jwtService;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID INTEGRATION_ID = UUID.randomUUID();

    private SyncJob sampleJob() {
        return SyncJob.create(UUID.randomUUID(), INTEGRATION_ID, TENANT_ID, "123-456-7890",
            "CAMPAIGN_PERFORMANCE",
            new DateWindow(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 4)),
            TriggerType.MANUAL, "user-1", "smoke test", Instant.now());
    }

    @Test
    void shouldReturn202OnManualSync() throws Exception {
        when(ingestionJobService.enqueueManualSync(any(), any(), anyString(), anyString(), anyString()))
            .thenReturn(sampleJob());

        mockMvc.perform(post("/api/v1/ingestion/jobs/manual")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Tenant-Id", TENANT_ID.toString())
                .content("""
                    {
                        "integrationId": "%s",
                        "accountId": "123-456-7890",
                        "reason": "smoke test"
                    }
                    """.formatted(INTEGRATION_ID)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void shouldReturn202OnBackfill() throws Exception {
        when(ingestionJobService.enqueueBackfill(any(), any(), anyString(), any(), anyString(), anyString()))
            .thenReturn(sampleJob());

        mockMvc.perform(post("/api/v1/ingestion/jobs/backfill")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Tenant-Id", TENANT_ID.toString())
                .content("""
                    {
                        "integrationId": "%s",
                        "accountId": "123-456-7890",
                        "dateFrom": "2026-02-01",
                        "dateTo": "2026-02-07",
                        "reason": "backfill missing data"
                    }
                    """.formatted(INTEGRATION_ID)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldReturn202OnForceRun() throws Exception {
        when(ingestionJobService.forceRun(any(), any(), anyString(), anyString(), anyString()))
            .thenReturn(sampleJob());

        mockMvc.perform(post("/api/v1/ingestion/jobs/force-run")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Tenant-Id", TENANT_ID.toString())
                .content("""
                    {
                        "integrationId": "%s",
                        "accountId": "123-456-7890",
                        "reason": "force after incident"
                    }
                    """.formatted(INTEGRATION_ID)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldReturn200WithJobList() throws Exception {
        SyncJobInfo info = new SyncJobInfo(
            UUID.randomUUID(), INTEGRATION_ID, "123-456-7890", "CAMPAIGN_PERFORMANCE",
            LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 4),
            "SUCCEEDED", 1, "DAILY", null, Instant.now()
        );
        when(ingestionQueryPort.listJobHistory(INTEGRATION_ID)).thenReturn(List.of(info));

        mockMvc.perform(get("/api/v1/ingestion/jobs")
                .param("integrationId", INTEGRATION_ID.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].status").value("SUCCEEDED"))
            .andExpect(jsonPath("$.data[0].reportType").value("CAMPAIGN_PERFORMANCE"));
    }
}
