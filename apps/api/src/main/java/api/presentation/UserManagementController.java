package api.presentation;

import api.ApiResponse;
import api.application.UserManagementService;
import api.application.dto.CreateUserRequest;
import api.application.dto.CreateUserResponse;
import api.application.dto.UserDetailResponse;
import api.application.dto.UserListItemResponse;
import api.security.RequestIdFilter;
import api.security.UserPrincipal;
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

  private final UserManagementService userManagementService;

  public UserManagementController(UserManagementService userManagementService) {
    this.userManagementService = userManagementService;
  }

  @GetMapping
  public ApiResponse<List<UserListItemResponse>> listUsers(
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request
  ) {
    return ApiResponse.success(userManagementService.listUsers(principal), requestId(request));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<CreateUserResponse>> createUser(
      @RequestBody(required = false) CreateUserRequest body,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request
  ) {
    CreateUserResponse result = userManagementService.createUser(body, principal);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(result, requestId(request)));
  }

  @GetMapping("/{id}")
  public ApiResponse<UserDetailResponse> getUser(
      @PathVariable UUID id,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request
  ) {
    return ApiResponse.success(userManagementService.getUser(id, principal), requestId(request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(
      @PathVariable UUID id,
      @AuthenticationPrincipal UserPrincipal principal
  ) {
    userManagementService.deleteUser(id, principal);
    return ResponseEntity.noContent().build();
  }

  private static String requestId(HttpServletRequest request) {
    Object value = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTR);
    return value == null ? "unknown" : value.toString();
  }
}
