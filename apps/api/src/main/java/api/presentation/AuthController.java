package api.presentation;

import api.ApiResponse;
import api.application.AuthService;
import api.application.AuthService.RefreshResult;
import api.application.dto.LoginRequest;
import api.application.dto.LoginResponse;
import api.application.dto.MeResponse;
import api.application.dto.MessageResponse;
import api.application.dto.SwitchTenantRequest;
import api.application.dto.TokenResponse;
import api.security.RequestIdFilter;
import api.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(8);
  private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(7);

  private final AuthService authService;
  private final boolean isProduction;

  public AuthController(
      AuthService authService,
      @Value("${app.env:development}") String environment
  ) {
    this.authService = authService;
    this.isProduction = "production".equalsIgnoreCase(environment);
  }

  @PostMapping("/login")
  public ApiResponse<LoginResponse> login(
      @RequestBody(required = false) LoginRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse response
  ) {
    LoginResponse result = authService.login(request);
    setAuthCookies(response, result.token(), result.refreshToken());
    return ApiResponse.success(result, requestId(httpRequest));
  }

  @PostMapping("/refresh")
  public ApiResponse<TokenResponse> refresh(
      @CookieValue(value = "refresh_token", required = false) String refreshToken,
      HttpServletRequest httpRequest,
      HttpServletResponse response
  ) {
    RefreshResult result = authService.refresh(refreshToken);
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
    TokenResponse tokenResponse = authService.switchTenant(principal, tenantId);
    setAccessCookie(response, tokenResponse.token());
    return ApiResponse.success(tokenResponse, requestId(httpRequest));
  }

  @GetMapping("/me")
  public ApiResponse<MeResponse> me(
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest httpRequest
  ) {
    return ApiResponse.success(authService.me(principal), requestId(httpRequest));
  }

  @PostMapping("/logout")
  public ApiResponse<MessageResponse> logout(
      @AuthenticationPrincipal UserPrincipal principal,
      @CookieValue(value = "refresh_token", required = false) String refreshToken,
      HttpServletRequest httpRequest,
      HttpServletResponse response
  ) {
    authService.logout(principal, refreshToken);
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
    Object value = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTR);
    return value == null ? "unknown" : value.toString();
  }
}
