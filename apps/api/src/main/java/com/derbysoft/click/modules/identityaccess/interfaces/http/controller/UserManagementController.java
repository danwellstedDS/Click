package com.derbysoft.click.modules.identityaccess.interfaces.http.controller;

import com.derbysoft.click.bootstrap.web.RequestContextFilter;
import com.derbysoft.click.modules.identityaccess.application.handlers.UserManagementHandler;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.CreateUserRequest;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.CreateUserResponse;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.UserDetailResponse;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.UserListItemResponse;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.UserPrincipal;
import com.derbysoft.click.sharedkernel.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserManagementController {

  private final UserManagementHandler userManagementHandler;

  public UserManagementController(UserManagementHandler userManagementHandler) {
    this.userManagementHandler = userManagementHandler;
  }

  @GetMapping
  public ApiResponse<List<UserListItemResponse>> listUsers(
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request
  ) {
    return ApiResponse.success(userManagementHandler.listUsers(principal), requestId(request));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<CreateUserResponse>> createUser(
      @RequestBody(required = false) CreateUserRequest body,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request
  ) {
    CreateUserResponse result = userManagementHandler.createUser(body, principal);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(result, requestId(request)));
  }

  @GetMapping("/{id}")
  public ApiResponse<UserDetailResponse> getUser(
      @PathVariable UUID id,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request
  ) {
    return ApiResponse.success(userManagementHandler.getUser(id, principal), requestId(request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(
      @PathVariable UUID id,
      @AuthenticationPrincipal UserPrincipal principal
  ) {
    userManagementHandler.deleteUser(id, principal);
    return ResponseEntity.noContent().build();
  }

  private static String requestId(HttpServletRequest request) {
    Object value = request.getAttribute(RequestContextFilter.REQUEST_ID_ATTR);
    return value == null ? "unknown" : value.toString();
  }
}
