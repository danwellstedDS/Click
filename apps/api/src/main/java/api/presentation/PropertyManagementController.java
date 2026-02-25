package api.presentation;

import api.ApiResponse;
import api.application.PropertyManagementService;
import api.application.dto.CreatePropertyRequest;
import api.application.dto.PropertyListItemResponse;
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
@RequestMapping("/api/v1/properties")
public class PropertyManagementController {

  private final PropertyManagementService propertyManagementService;

  public PropertyManagementController(PropertyManagementService propertyManagementService) {
    this.propertyManagementService = propertyManagementService;
  }

  @GetMapping
  public ApiResponse<List<PropertyListItemResponse>> listProperties(
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request
  ) {
    return ApiResponse.success(propertyManagementService.listProperties(principal), requestId(request));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<PropertyListItemResponse>> createProperty(
      @RequestBody(required = false) CreatePropertyRequest body,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request
  ) {
    PropertyListItemResponse result = propertyManagementService.createProperty(body, principal);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(result, requestId(request)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProperty(
      @PathVariable UUID id,
      @AuthenticationPrincipal UserPrincipal principal
  ) {
    propertyManagementService.deleteProperty(id, principal);
    return ResponseEntity.noContent().build();
  }

  private static String requestId(HttpServletRequest request) {
    Object value = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTR);
    return value == null ? "unknown" : value.toString();
  }
}
