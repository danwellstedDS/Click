package com.derbysoft.click.modules.googleadsmanagement.interfaces.http.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.derbysoft.click.modules.googleadsmanagement.application.handlers.DiscoverAccountsHandler;
import com.derbysoft.click.modules.googleadsmanagement.application.handlers.GoogleConnectionService;
import com.derbysoft.click.modules.googleadsmanagement.application.ports.GoogleAdsApiPort;
import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.GoogleConnection;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository.AccountGraphStateRepository;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.JwtService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GoogleConnectionController.class)
@AutoConfigureMockMvc(addFilters = false)
class GoogleConnectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GoogleConnectionService connectionService;

    @MockitoBean
    private DiscoverAccountsHandler discoverAccountsHandler;

    @MockitoBean
    private AccountGraphStateRepository graphStateRepository;

    @MockitoBean
    private GoogleAdsApiPort googleAdsApiPort;

    @MockitoBean
    private JwtService jwtService;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID CONNECTION_ID = UUID.randomUUID();

    private static GoogleConnection sampleConnection() {
        return GoogleConnection.create(CONNECTION_ID, TENANT_ID, "858-270-7576",
            "infra/secrets/creds.json", Instant.now());
    }

    @Test
    void shouldReturn201OnCreateConnection() throws Exception {
        when(connectionService.createConnection(any(), any(), any())).thenReturn(sampleConnection());

        mockMvc.perform(post("/api/v1/google/connections")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "tenantId": "%s",
                        "managerId": "858-270-7576",
                        "credentialPath": "infra/secrets/google-search-creds.json"
                    }
                    """.formatted(TENANT_ID)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.managerId").value("858-270-7576"))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void shouldReturn200WithConnectionForTenant() throws Exception {
        when(connectionService.findByTenantId(TENANT_ID)).thenReturn(Optional.of(sampleConnection()));

        mockMvc.perform(get("/api/v1/google/connections")
                .param("tenantId", TENANT_ID.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.managerId").value("858-270-7576"));
    }

    @Test
    void shouldReturn202OnDiscoverAccounts() throws Exception {
        doNothing().when(discoverAccountsHandler).discover(CONNECTION_ID);
        when(connectionService.findById(CONNECTION_ID)).thenReturn(sampleConnection());

        mockMvc.perform(post("/api/v1/google/connections/{id}/discover", CONNECTION_ID))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldReturn200WithAccountGraph() throws Exception {
        when(graphStateRepository.findByConnectionId(CONNECTION_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/google/connections/{id}/accounts", CONNECTION_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldReturn200WithHealthyTrueWhenCredentialValid() throws Exception {
        when(connectionService.findById(CONNECTION_ID)).thenReturn(sampleConnection());
        when(googleAdsApiPort.validateCredential(any(), any())).thenReturn(true);

        mockMvc.perform(post("/api/v1/google/connections/{id}/validate", CONNECTION_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.healthy").value(true))
            .andExpect(jsonPath("$.data.managerId").value("858-270-7576"))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void shouldReturn200WithHealthyFalseWhenCredentialInvalid() throws Exception {
        when(connectionService.findById(CONNECTION_ID)).thenReturn(sampleConnection());
        when(googleAdsApiPort.validateCredential(any(), any())).thenReturn(false);

        mockMvc.perform(post("/api/v1/google/connections/{id}/validate", CONNECTION_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.healthy").value(false));
    }

    @Test
    void shouldReturn204OnDeleteConnection() throws Exception {
        when(connectionService.findById(CONNECTION_ID)).thenReturn(sampleConnection());

        mockMvc.perform(delete("/api/v1/google/connections/{id}", CONNECTION_ID))
            .andExpect(status().isNoContent());
    }
}
