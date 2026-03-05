package com.derbysoft.click.modules.googleadsmanagement.interfaces.http.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.derbysoft.click.modules.googleadsmanagement.application.handlers.AccountBindingService;
import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.AccountBinding;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.BindingType;
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

@WebMvcTest(AccountBindingController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountBindingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountBindingService bindingService;

    @MockitoBean
    private JwtService jwtService;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID CONNECTION_ID = UUID.randomUUID();
    private static final UUID BINDING_ID = UUID.randomUUID();
    private static final String CUSTOMER_ID = "506-204-8043";

    private static AccountBinding sampleBinding() {
        return AccountBinding.create(BINDING_ID, CONNECTION_ID, TENANT_ID,
            CUSTOMER_ID, BindingType.OWNED, Instant.now());
    }

    @Test
    void shouldReturn201OnCreateBinding() throws Exception {
        when(bindingService.createBinding(any(), any(), any())).thenReturn(sampleBinding());

        mockMvc.perform(post("/api/v1/google/bindings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "connectionId": "%s",
                        "customerId": "%s",
                        "bindingType": "OWNED"
                    }
                    """.formatted(CONNECTION_ID, CUSTOMER_ID)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.customerId").value(CUSTOMER_ID))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void shouldReturn200WithBindingsForConnection() throws Exception {
        when(bindingService.findByConnectionId(CONNECTION_ID)).thenReturn(List.of(sampleBinding()));

        mockMvc.perform(get("/api/v1/google/bindings")
                .param("connectionId", CONNECTION_ID.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].customerId").value(CUSTOMER_ID));
    }

    @Test
    void shouldReturn200WithResolvedActiveAccounts() throws Exception {
        when(bindingService.resolveApplicableAccounts(TENANT_ID)).thenReturn(List.of(sampleBinding()));

        mockMvc.perform(get("/api/v1/google/bindings/resolve")
                .param("tenantId", TENANT_ID.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
    }

    @Test
    void shouldReturn204OnRemoveBinding() throws Exception {
        when(bindingService.removeBinding(BINDING_ID)).thenReturn(sampleBinding());

        mockMvc.perform(delete("/api/v1/google/bindings/{id}", BINDING_ID))
            .andExpect(status().isNoContent());
    }
}
