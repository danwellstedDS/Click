package com.derbysoft.click.modules.identityaccess.interfaces.http.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.derbysoft.click.modules.identityaccess.application.handlers.AuthCommandHandler;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.JwtService;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.LoginResponse;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.TenantInfo;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.UserInfo;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AuthCommandHandler authCommandHandler;

  @MockitoBean
  private JwtService jwtService;

  @Test
  void shouldReturnLoginResponse() throws Exception {
    var response = new LoginResponse(
        "jwt-token",
        "refresh-token",
        new UserInfo("user-id", "user@example.com"),
        List.of(new TenantInfo("tenant-id", "ADMIN"))
    );

    when(authCommandHandler.login(any())).thenReturn(response);

    mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"user@example.com\",\"password\":\"password\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.token").value("jwt-token"));
  }
}
