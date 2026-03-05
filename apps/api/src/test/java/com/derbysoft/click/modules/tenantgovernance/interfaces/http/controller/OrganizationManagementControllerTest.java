package com.derbysoft.click.modules.tenantgovernance.interfaces.http.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.derbysoft.click.modules.identityaccess.infrastructure.security.JwtService;
import com.derbysoft.click.modules.tenantgovernance.domain.OrganizationRepository;
import com.derbysoft.click.modules.tenantgovernance.domain.aggregates.Organization;
import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.OrganizationType;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrganizationManagementController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrganizationManagementControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private OrganizationRepository organizationRepository;

  @MockitoBean
  private JwtService jwtService;

  private static final UUID ORG_ID = UUID.randomUUID();

  private static Organization sampleOrg() {
    return Organization.create(ORG_ID, "Marriott Hotels", OrganizationType.CHAIN, Instant.now(), Instant.now());
  }

  @Test
  void shouldReturn200WithOrganisationList() throws Exception {
    when(organizationRepository.findAll()).thenReturn(List.of(sampleOrg()));

    mockMvc.perform(get("/api/v1/organizations"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].name").value("Marriott Hotels"))
        .andExpect(jsonPath("$.data[0].type").value("CHAIN"));
  }

  @Test
  void shouldReturn403WhenNotAdmin() throws Exception {
    when(organizationRepository.findAll()).thenThrow(new DomainError.Forbidden("AUTH_403", "Admin access required"));

    mockMvc.perform(get("/api/v1/organizations"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));
  }
}
