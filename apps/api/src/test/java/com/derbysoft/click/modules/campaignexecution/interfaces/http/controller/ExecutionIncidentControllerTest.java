package com.derbysoft.click.modules.campaignexecution.interfaces.http.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.derbysoft.click.modules.campaignexecution.api.contracts.ExecutionIncidentSummary;
import com.derbysoft.click.modules.campaignexecution.api.ports.CampaignManagementQueryPort;
import com.derbysoft.click.modules.campaignexecution.application.handlers.AcknowledgeEscalationHandler;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.ExecutionIncident;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
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

@WebMvcTest(ExecutionIncidentController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExecutionIncidentControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private CampaignManagementQueryPort queryPort;
    @MockitoBean private AcknowledgeEscalationHandler acknowledgeHandler;
    @MockitoBean private JwtService jwtService;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID INCIDENT_ID = UUID.randomUUID();
    private static final String KEY = "revision-1:item-1:CREATE_CAMPAIGN:0";
    private static final Instant NOW = Instant.parse("2026-03-06T09:00:00Z");

    private ExecutionIncidentSummary openSummary() {
        return new ExecutionIncidentSummary(INCIDENT_ID, KEY, TENANT_ID,
            "TRANSIENT", "OPEN", 1, NOW, null, null);
    }

    private ExecutionIncidentSummary escalatedSummary() {
        return new ExecutionIncidentSummary(INCIDENT_ID, KEY, TENANT_ID,
            "TRANSIENT", "ESCALATED", 3, NOW, null, null);
    }

    private ExecutionIncident escalatedAggregate() {
        ExecutionIncident incident = ExecutionIncident.open(INCIDENT_ID, KEY, TENANT_ID,
            FailureClass.TRANSIENT, NOW);
        incident.recordFailure(NOW);
        incident.recordFailure(NOW);
        incident.clearEvents();
        return incident;
    }

    @Test
    void shouldReturn200WithOpenIncidents() throws Exception {
        when(queryPort.listOpenIncidents(TENANT_ID)).thenReturn(List.of(openSummary()));

        mockMvc.perform(get("/api/v1/execution/incidents")
                .param("tenantId", TENANT_ID.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].status").value("OPEN"))
            .andExpect(jsonPath("$.data[0].consecutiveFailures").value(1));
    }

    @Test
    void shouldReturn200WithEscalatedIncidents() throws Exception {
        when(queryPort.listEscalatedIncidents(TENANT_ID)).thenReturn(List.of(escalatedSummary()));

        mockMvc.perform(get("/api/v1/execution/incidents/escalated")
                .param("tenantId", TENANT_ID.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].status").value("ESCALATED"))
            .andExpect(jsonPath("$.data[0].consecutiveFailures").value(3));
    }

    @Test
    void shouldReturn200OnAcknowledge() throws Exception {
        ExecutionIncident acknowledged = escalatedAggregate();
        acknowledged.acknowledge("confirmed root cause", "ops-user", NOW);

        when(acknowledgeHandler.acknowledge(any(), anyString(), anyString()))
            .thenReturn(acknowledged);

        mockMvc.perform(post("/api/v1/execution/incidents/{id}/acknowledge", INCIDENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"ackReason": "confirmed root cause"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("ESCALATED"))
            .andExpect(jsonPath("$.data.acknowledgedBy").value("ops-user"));
    }
}
