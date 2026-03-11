package com.derbysoft.click.modules.attributionmapping.interfaces.http.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.derbysoft.click.modules.attributionmapping.domain.MappingOverrideRepository;
import com.derbysoft.click.modules.attributionmapping.domain.aggregates.MappingOverride;
import com.derbysoft.click.modules.attributionmapping.domain.valueobjects.OverrideScope;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappingOverrideEntity;
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

@WebMvcTest(MappingOverrideController.class)
@AutoConfigureMockMvc(addFilters = false)
class MappingOverrideControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean MappingOverrideRepository overrideRepository;
    @MockitoBean JwtService jwtService;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID TARGET_NODE_ID = UUID.randomUUID();
    private static final UUID OVERRIDE_ID = UUID.randomUUID();

    private MappingOverrideEntity sampleEntity() {
        MappingOverrideEntity e = new MappingOverrideEntity();
        e.setId(OVERRIDE_ID);
        e.setTenantId(TENANT_ID);
        e.setScopeType("ACCOUNT");
        e.setCustomerAccountId("123");
        e.setCampaignId(null);
        e.setTargetOrgNodeId(TARGET_NODE_ID);
        e.setTargetScopeType("Property");
        e.setReason("manual fix");
        e.setActor("system");
        e.setStatus("ACTIVE");
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        return e;
    }

    @Test
    void shouldListActiveOverrides() throws Exception {
        when(overrideRepository.findActiveByTenantId(TENANT_ID))
            .thenReturn(List.of(sampleEntity()));

        mockMvc.perform(get("/api/mapping-overrides")
                .header("X-Tenant-Id", TENANT_ID.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].scopeType").value("ACCOUNT"))
            .andExpect(jsonPath("$.data[0].customerAccountId").value("123"));
    }

    @Test
    void shouldCreateOverrideForAccount() throws Exception {
        MappingOverride saved = MappingOverride.set(
            TENANT_ID, OverrideScope.ACCOUNT, "123", null,
            TARGET_NODE_ID, "Property", "manual fix", "system", Instant.now());
        when(overrideRepository.save(any())).thenReturn(saved);

        mockMvc.perform(post("/api/mapping-overrides")
                .header("X-Tenant-Id", TENANT_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "customerAccountId": "123",
                        "targetOrgNodeId": "%s",
                        "targetScopeType": "Property",
                        "reason": "manual fix"
                    }
                    """.formatted(TARGET_NODE_ID)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.scopeType").value("ACCOUNT"));
    }

    @Test
    void shouldRemoveOverride() throws Exception {
        MappingOverride override = MappingOverride.set(
            TENANT_ID, OverrideScope.ACCOUNT, "123", null,
            TARGET_NODE_ID, "Property", "reason", "system", Instant.now());
        when(overrideRepository.findById(OVERRIDE_ID)).thenReturn(Optional.of(override));
        when(overrideRepository.save(any())).thenReturn(override);

        mockMvc.perform(delete("/api/mapping-overrides/{id}", OVERRIDE_ID)
                .header("X-Tenant-Id", TENANT_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"no longer needed\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldReturn404WhenOverrideNotFound() throws Exception {
        when(overrideRepository.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/mapping-overrides/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"not found\"}"))
            .andExpect(status().isNotFound());
    }
}
