package com.derbysoft.click.modules.organisationstructure.interfaces.http.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.derbysoft.click.modules.identityaccess.infrastructure.security.JwtService;
import com.derbysoft.click.modules.organisationstructure.application.handlers.ChainManagementService;
import com.derbysoft.click.modules.organisationstructure.domain.aggregates.PropertyGroup;
import com.derbysoft.click.modules.organisationstructure.domain.valueobjects.ChainStatus;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
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

@WebMvcTest(ChainManagementController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChainManagementControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ChainManagementService chainManagementService;

  @MockitoBean
  private JwtService jwtService;

  private static final UUID CHAIN_ID = UUID.randomUUID();

  private static PropertyGroup sampleChain() {
    return PropertyGroup.reconstitute(CHAIN_ID, null, "Demo Chain", "UTC", "USD", null, Instant.now(), Instant.now(), ChainStatus.ACTIVE);
  }

  @Test
  void shouldReturn200WithChainList() throws Exception {
    when(chainManagementService.listChains(any())).thenReturn(List.of(sampleChain()));

    mockMvc.perform(get("/api/v1/chains"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].name").value("Demo Chain"))
        .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
  }

  @Test
  void shouldReturn201WhenChainCreated() throws Exception {
    when(chainManagementService.createChain(any(), any(), any(), any(), any())).thenReturn(sampleChain());

    mockMvc.perform(post("/api/v1/chains")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Demo Chain\",\"timezone\":\"UTC\",\"currency\":\"USD\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.name").value("Demo Chain"))
        .andExpect(jsonPath("$.data.status").value("ACTIVE"));
  }

  @Test
  void shouldReturn400WhenNameMissing() throws Exception {
    when(chainManagementService.createChain(any(), any(), any(), any()))
        .thenThrow(new DomainError.ValidationError("CHAIN_400", "name is required"));

    mockMvc.perform(post("/api/v1/chains")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void shouldReturn200WhenStatusUpdated() throws Exception {
    PropertyGroup inactive = PropertyGroup.reconstitute(CHAIN_ID, null, "Demo Chain", "UTC", "USD", null, Instant.now(), Instant.now(), ChainStatus.INACTIVE);
    when(chainManagementService.updateStatus(any(), any(), any())).thenReturn(inactive);

    mockMvc.perform(patch("/api/v1/chains/" + CHAIN_ID + "/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"INACTIVE\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.status").value("INACTIVE"));
  }
}
