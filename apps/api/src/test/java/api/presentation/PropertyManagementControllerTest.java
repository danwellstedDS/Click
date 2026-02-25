package api.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import api.application.AuthException;
import api.application.PropertyManagementService;
import api.application.dto.PropertyListItemResponse;
import api.security.JwtService;
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
  private PropertyManagementService propertyManagementService;

  @MockitoBean
  private JwtService jwtService;

  private static final UUID PROPERTY_ID = UUID.randomUUID();

  private static PropertyListItemResponse sampleProperty() {
    return new PropertyListItemResponse(PROPERTY_ID, "Demo Property", true, null, Instant.now());
  }

  @Test
  void listProperties_returns200WithList() throws Exception {
    when(propertyManagementService.listProperties(any())).thenReturn(List.of(sampleProperty()));

    mockMvc.perform(get("/api/v1/properties"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].name").value("Demo Property"))
        .andExpect(jsonPath("$.data[0].isActive").value(true));
  }

  @Test
  void createProperty_returns201() throws Exception {
    when(propertyManagementService.createProperty(any(), any())).thenReturn(sampleProperty());

    mockMvc.perform(post("/api/v1/properties")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Demo Property\",\"isActive\":true}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.name").value("Demo Property"));
  }

  @Test
  void deleteProperty_returns204() throws Exception {
    doNothing().when(propertyManagementService).deleteProperty(any(), any());

    mockMvc.perform(delete("/api/v1/properties/" + PROPERTY_ID))
        .andExpect(status().isNoContent());
  }

  @Test
  void listProperties_returns403ForNonAdmin() throws Exception {
    doThrow(new AuthException("AUTH_403", "Admin access required", 403))
        .when(propertyManagementService).listProperties(any());

    mockMvc.perform(get("/api/v1/properties"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("AUTH_403"));
  }
}
