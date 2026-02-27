package com.derbysoft.click.modules.identityaccess.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class ApiAuthEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException
  ) throws IOException {
    Object value = request.getAttribute("requestId");
    String requestId = value == null ? "unknown" : value.toString();

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(
        "{\"success\":false,\"data\":null,\"error\":{\"code\":\"AUTH_001\",\"message\":\"Missing or invalid token\"},\"meta\":{\"requestId\":\"%s\"}}".formatted(requestId)
    );
  }
}
