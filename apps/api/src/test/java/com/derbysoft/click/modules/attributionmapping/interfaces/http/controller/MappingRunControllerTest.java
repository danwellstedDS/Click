package com.derbysoft.click.modules.attributionmapping.interfaces.http.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.derbysoft.click.modules.attributionmapping.api.contracts.MappedFactInfo;
import com.derbysoft.click.modules.attributionmapping.api.contracts.MappingRunInfo;
import com.derbysoft.click.modules.attributionmapping.api.ports.AttributionQueryPort;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.JwtService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MappingRunController.class)
@AutoConfigureMockMvc(addFilters = false)
class MappingRunControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AttributionQueryPort attributionQueryPort;
    @MockitoBean JwtService jwtService;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID RUN_ID = UUID.randomUUID();
    private static final UUID BATCH_ID = UUID.randomUUID();

    private MappingRunInfo sampleRun() {
        return new MappingRunInfo(
            RUN_ID, BATCH_ID, TENANT_ID, "v1", "abc123",
            "PRODUCED", 90, 0, 0,
            Instant.now(), Instant.now(), null, null, Instant.now()
        );
    }

    private MappedFactInfo sampleFact() {
        return new MappedFactInfo(
            UUID.randomUUID(), RUN_ID, UUID.randomUUID(), TENANT_ID,
            UUID.randomUUID(), "Property", "HIGH", new BigDecimal("0.900"),
            "EXPLICIT_BINDING", "v1", false, Instant.now()
        );
    }

    @Test
    void shouldListRuns() throws Exception {
        when(attributionQueryPort.listRuns(any(), anyInt(), anyInt()))
            .thenReturn(List.of(sampleRun()));

        mockMvc.perform(get("/api/mapping-runs")
                .header("X-Tenant-Id", TENANT_ID.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].status").value("PRODUCED"))
            .andExpect(jsonPath("$.data[0].mappedCount").value(90));
    }

    @Test
    void shouldGetRunById() throws Exception {
        when(attributionQueryPort.findRunById(RUN_ID))
            .thenReturn(Optional.of(sampleRun()));

        mockMvc.perform(get("/api/mapping-runs/{runId}", RUN_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.ruleSetVersion").value("v1"));
    }

    @Test
    void shouldReturn404WhenRunNotFound() throws Exception {
        when(attributionQueryPort.findRunById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mapping-runs/{runId}", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldListFacts() throws Exception {
        when(attributionQueryPort.listFacts(any(), anyInt(), anyInt()))
            .thenReturn(List.of(sampleFact()));

        mockMvc.perform(get("/api/mapping-runs/{runId}/facts", RUN_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].confidenceBand").value("HIGH"));
    }

    @Test
    void shouldListLowConfidenceFacts() throws Exception {
        MappedFactInfo unresolvedFact = new MappedFactInfo(
            UUID.randomUUID(), RUN_ID, UUID.randomUUID(), TENANT_ID,
            null, null, "UNRESOLVED", BigDecimal.ZERO,
            "NO_MATCH", "v1", false, Instant.now()
        );
        when(attributionQueryPort.listLowConfidence(any(), anyInt(), anyInt()))
            .thenReturn(List.of(unresolvedFact));

        mockMvc.perform(get("/api/mapping-runs/{runId}/low-confidence", RUN_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].confidenceBand").value("UNRESOLVED"))
            .andExpect(jsonPath("$.data[0].resolutionReasonCode").value("NO_MATCH"));
    }
}
