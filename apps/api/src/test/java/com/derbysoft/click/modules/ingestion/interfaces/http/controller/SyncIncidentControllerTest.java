package com.derbysoft.click.modules.ingestion.interfaces.http.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.derbysoft.click.modules.identityaccess.infrastructure.security.JwtService;
import com.derbysoft.click.modules.ingestion.api.contracts.SyncIncidentInfo;
import com.derbysoft.click.modules.ingestion.api.ports.IngestionQueryPort;
import com.derbysoft.click.modules.ingestion.application.handlers.IngestionJobService;
import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncIncident;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.IncidentStatus;
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

@WebMvcTest(SyncIncidentController.class)
@AutoConfigureMockMvc(addFilters = false)
class SyncIncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngestionJobService ingestionJobService;

    @MockitoBean
    private IngestionQueryPort ingestionQueryPort;

    @MockitoBean
    private JwtService jwtService;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID INCIDENT_ID = UUID.randomUUID();
    private static final String KEY = "integration-1:acct:2026-03-01:2026-03-04:CAMPAIGN_PERFORMANCE";

    private SyncIncidentInfo openInfo() {
        return new SyncIncidentInfo(INCIDENT_ID, KEY, TENANT_ID,
            "TRANSIENT", "OPEN", 1, Instant.now(), null, null);
    }

    private SyncIncidentInfo escalatedInfo() {
        return new SyncIncidentInfo(INCIDENT_ID, KEY, TENANT_ID,
            "TRANSIENT", "ESCALATED", 3, Instant.now(), null, null);
    }

    private SyncIncident escalatedAggregate() {
        SyncIncident incident = SyncIncident.open(INCIDENT_ID, KEY, TENANT_ID,
            FailureClass.TRANSIENT, Instant.now());
        incident.recordFailure(Instant.now());
        incident.recordFailure(Instant.now());
        incident.clearEvents();
        return incident;
    }

    @Test
    void shouldReturn200WithOpenIncidents() throws Exception {
        when(ingestionQueryPort.listOpenIncidents(TENANT_ID)).thenReturn(List.of(openInfo()));

        mockMvc.perform(get("/api/v1/ingestion/incidents")
                .param("tenantId", TENANT_ID.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].status").value("OPEN"))
            .andExpect(jsonPath("$.data[0].consecutiveFailures").value(1));
    }

    @Test
    void shouldReturn200WithEscalatedIncidents() throws Exception {
        when(ingestionQueryPort.listEscalatedIncidents(TENANT_ID)).thenReturn(List.of(escalatedInfo()));

        mockMvc.perform(get("/api/v1/ingestion/incidents/escalated")
                .param("tenantId", TENANT_ID.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].status").value("ESCALATED"))
            .andExpect(jsonPath("$.data[0].consecutiveFailures").value(3));
    }

    @Test
    void shouldReturn200OnAcknowledge() throws Exception {
        SyncIncident acknowledged = escalatedAggregate();
        acknowledged.acknowledge("confirmed root cause", "ops-user", Instant.now());

        when(ingestionJobService.acknowledgeEscalation(any(), anyString(), anyString()))
            .thenReturn(acknowledged);

        mockMvc.perform(post("/api/v1/ingestion/incidents/{id}/acknowledge", INCIDENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "ackReason": "confirmed root cause"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("ESCALATED"))
            .andExpect(jsonPath("$.data.acknowledgedBy").value("ops-user"));
    }
}
