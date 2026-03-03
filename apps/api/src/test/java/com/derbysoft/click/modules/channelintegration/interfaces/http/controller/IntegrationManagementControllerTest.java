package com.derbysoft.click.modules.channelintegration.interfaces.http.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.derbysoft.click.modules.channelintegration.application.handlers.IntegrationService;
import com.derbysoft.click.modules.channelintegration.domain.aggregates.IntegrationInstance;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.CredentialRef;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.SyncSchedule;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.JwtService;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(IntegrationManagementController.class)
@AutoConfigureMockMvc(addFilters = false)
class IntegrationManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IntegrationService integrationService;

    @MockitoBean
    private JwtService jwtService;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID INTEGRATION_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();

    private static IntegrationInstance sampleInstance() {
        IntegrationInstance instance = IntegrationInstance.create(
            TENANT_ID,
            Channel.GOOGLE_ADS,
            "default",
            SyncSchedule.cron("0 * * * *", "UTC")
        );
        instance.attachCredential(new CredentialRef(UUID.randomUUID()), ACTOR_ID);
        instance.clearEvents();
        return instance;
    }

    @Test
    void createIntegration_returns201() throws Exception {
        when(integrationService.createIntegrationInstance(any(), any(), any(), any()))
            .thenReturn(sampleInstance());

        mockMvc.perform(post("/api/v1/integrations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "tenantId": "%s",
                        "channel": "GOOGLE_ADS",
                        "cadenceType": "CRON",
                        "cronExpression": "0 * * * *",
                        "timezone": "UTC"
                    }
                    """.formatted(TENANT_ID)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.channel").value("GOOGLE_ADS"))
            .andExpect(jsonPath("$.data.status").value("Active"));
    }

    @Test
    void createIntegration_withIntervalCadence_returns201() throws Exception {
        IntegrationInstance instance = IntegrationInstance.create(
            TENANT_ID, Channel.META_ADS, "default", SyncSchedule.interval(30, "UTC")
        );
        when(integrationService.createIntegrationInstance(any(), any(), any(), any()))
            .thenReturn(instance);

        mockMvc.perform(post("/api/v1/integrations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "tenantId": "%s",
                        "channel": "META_ADS",
                        "cadenceType": "INTERVAL",
                        "intervalMinutes": 30,
                        "timezone": "UTC"
                    }
                    """.formatted(TENANT_ID)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.cadenceType").value("INTERVAL"))
            .andExpect(jsonPath("$.data.intervalMinutes").value(30));
    }

    @Test
    void runSync_returns202() throws Exception {
        IntegrationInstance instance = sampleInstance();
        when(integrationService.runSyncNow(any())).thenReturn(instance);

        mockMvc.perform(post("/api/v1/integrations/{id}/sync", INTEGRATION_ID))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createIntegration_returns403WhenGovernanceDenies() throws Exception {
        when(integrationService.createIntegrationInstance(any(), any(), any(), any()))
            .thenThrow(new DomainError.Forbidden("GOV_403", "Integration creation not permitted"));

        mockMvc.perform(post("/api/v1/integrations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "tenantId": "%s",
                        "channel": "GOOGLE_ADS",
                        "cadenceType": "CRON",
                        "cronExpression": "0 * * * *",
                        "timezone": "UTC"
                    }
                    """.formatted(TENANT_ID)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("GOV_403"));
    }

    @Test
    void getIntegration_returns404WhenNotFound() throws Exception {
        when(integrationService.findById(any()))
            .thenThrow(new DomainError.NotFound("INT_404", "IntegrationInstance not found: " + INTEGRATION_ID));

        mockMvc.perform(get("/api/v1/integrations/{id}", INTEGRATION_ID))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INT_404"));
    }

    @Test
    void detachCredential_returns204() throws Exception {
        when(integrationService.detachCredential(any())).thenReturn(sampleInstance());

        mockMvc.perform(delete("/api/v1/integrations/{id}/credentials", INTEGRATION_ID))
            .andExpect(status().isNoContent());
    }
}
