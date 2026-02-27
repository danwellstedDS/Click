package com.derbysoft.click.modules.organisationstructure.interfaces.http.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.derbysoft.click.modules.identityaccess.infrastructure.security.JwtService;
import com.derbysoft.click.modules.organisationstructure.application.handlers.PropertyManagementHandler;
import com.derbysoft.click.modules.organisationstructure.interfaces.http.dto.PropertyListItemResponse;
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

@WebMvcTest(PropertyManagementController.class)
@AutoConfigureMockMvc(addFilters = false)
class PropertyManagementControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private PropertyManagementHandler propertyManagementHandler;

  @MockitoBean
  private JwtService jwtService;

  private static final UUID PROPERTY_ID = UUID.randomUUID();

  private static PropertyListItemResponse sampleProperty() {
    return new PropertyListItemResponse(PROPERTY_ID, "Demo Property", true, null, Instant.now());
  }

  @Test
  void listProperties_returns200WithList() throws Exception {
    when(propertyManagementHandler.listProperties(any())).thenReturn(List.of(sampleProperty()));

    mockMvc.perform(get("/api/v1/properties"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].name").value("Demo Property"))
        .andExpect(jsonPath("$.data[0].isActive").value(true));
  }

  @Test
  void createProperty_returns201() throws Exception {
    when(propertyManagementHandler.createProperty(any(), any())).thenReturn(sampleProperty());

    mockMvc.perform(post("/api/v1/properties")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Demo Property\",\"isActive\":true}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.name").value("Demo Property"));
  }

  @Test
  void deleteProperty_returns204() throws Exception {
    doNothing().when(propertyManagementHandler).deleteProperty(any(), any());

    mockMvc.perform(delete("/api/v1/properties/" + PROPERTY_ID))
        .andExpect(status().isNoContent());
  }

  @Test
  void listProperties_returns403ForNonAdmin() throws Exception {
    doThrow(new DomainError.Forbidden("AUTH_403", "Admin access required"))
        .when(propertyManagementHandler).listProperties(any());

    mockMvc.perform(get("/api/v1/properties"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("AUTH_403"));
  }
}
