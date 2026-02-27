package com.derbysoft.click.modules.identityaccess.interfaces.http.controller;

import com.derbysoft.click.bootstrap.web.RequestContextFilter;
import com.derbysoft.click.modules.identityaccess.application.handlers.AuthCommandHandler;
import com.derbysoft.click.modules.identityaccess.application.handlers.AuthCommandHandler.RefreshResult;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.LoginRequest;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.LoginResponse;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.MeResponse;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.MessageResponse;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.SwitchTenantRequest;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.TokenResponse;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.UserPrincipal;
import com.derbysoft.click.sharedkernel.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(8);
  private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(7);

  private final AuthCommandHandler authCommandHandler;
  private final boolean isProduction;

  public AuthController(
      AuthCommandHandler authCommandHandler,
      @Value("${app.env:development}") String environment
  ) {
    this.authCommandHandler = authCommandHandler;
    this.isProduction = "production".equalsIgnoreCase(environment);
  }

  @PostMapping("/login")
  public ApiResponse<LoginResponse> login(
      @RequestBody(required = false) LoginRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse response
  ) {
    LoginResponse result = authCommandHandler.login(request);
    setAuthCookies(response, result.token(), result.refreshToken());
    return ApiResponse.success(result, requestId(httpRequest));
  }

  @PostMapping("/refresh")
  public ApiResponse<TokenResponse> refresh(
      @CookieValue(value = "refresh_token", required = false) String refreshToken,
      HttpServletRequest httpRequest,
      HttpServletResponse response
  ) {
    RefreshResult result = authCommandHandler.refresh(refreshToken);
    setAuthCookies(response, result.response().token(), result.refreshToken());
    return ApiResponse.success(result.response(), requestId(httpRequest));
  }

  @PostMapping("/switch-tenant")
  public ApiResponse<TokenResponse> switchTenant(
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestBody(required = false) SwitchTenantRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse response
  ) {
    String tenantId = request == null ? null : request.tenantId();
    TokenResponse tokenResponse = authCommandHandler.switchTenant(principal, tenantId);
    setAccessCookie(response, tokenResponse.token());
    return ApiResponse.success(tokenResponse, requestId(httpRequest));
  }

  @GetMapping("/me")
  public ApiResponse<MeResponse> me(
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest httpRequest
  ) {
    return ApiResponse.success(authCommandHandler.me(principal), requestId(httpRequest));
  }

  @PostMapping("/logout")
  public ApiResponse<MessageResponse> logout(
      @AuthenticationPrincipal UserPrincipal principal,
      @CookieValue(value = "refresh_token", required = false) String refreshToken,
      HttpServletRequest httpRequest,
      HttpServletResponse response
  ) {
    authCommandHandler.logout(principal, refreshToken);
    clearAuthCookies(response);
    return ApiResponse.success(new MessageResponse("Logged out"), requestId(httpRequest));
  }

  private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
    setAccessCookie(response, accessToken);
    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
        .httpOnly(true)
        .secure(isProduction)
        .maxAge(REFRESH_TOKEN_DURATION)
        .path("/")
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
  }

  private void setAccessCookie(HttpServletResponse response, String accessToken) {
    ResponseCookie authCookie = ResponseCookie.from("auth_token", accessToken)
        .httpOnly(true)
        .secure(isProduction)
        .maxAge(ACCESS_TOKEN_DURATION)
        .path("/")
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, authCookie.toString());
  }

  private void clearAuthCookies(HttpServletResponse response) {
    ResponseCookie authCookie = ResponseCookie.from("auth_token", "")
        .httpOnly(true)
        .secure(isProduction)
        .maxAge(Duration.ZERO)
        .path("/")
        .build();
    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
        .httpOnly(true)
        .secure(isProduction)
        .maxAge(Duration.ZERO)
        .path("/")
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, authCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
  }

  private static String requestId(HttpServletRequest request) {
    Object value = request.getAttribute(RequestContextFilter.REQUEST_ID_ATTR);
    return value == null ? "unknown" : value.toString();
  }
}
