package com.derbysoft.click.modules.organisationstructure.interfaces.http.controller;

import com.derbysoft.click.modules.identityaccess.infrastructure.security.UserPrincipal;
import com.derbysoft.click.modules.organisationstructure.application.handlers.PropertyManagementHandler;
import com.derbysoft.click.modules.organisationstructure.interfaces.http.dto.CreatePropertyRequest;
import com.derbysoft.click.modules.organisationstructure.interfaces.http.dto.PropertyListItemResponse;
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
@RequestMapping("/api/v1/properties")
public class PropertyManagementController {

  private final PropertyManagementHandler propertyManagementHandler;

  public PropertyManagementController(PropertyManagementHandler propertyManagementHandler) {
    this.propertyManagementHandler = propertyManagementHandler;
  }

  @GetMapping
  public ApiResponse<List<PropertyListItemResponse>> listProperties(
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request
  ) {
    return ApiResponse.success(propertyManagementHandler.listProperties(principal), requestId(request));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<PropertyListItemResponse>> createProperty(
      @RequestBody(required = false) CreatePropertyRequest body,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request
  ) {
    PropertyListItemResponse result = propertyManagementHandler.createProperty(body, principal);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(result, requestId(request)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProperty(
      @PathVariable UUID id,
      @AuthenticationPrincipal UserPrincipal principal
  ) {
    propertyManagementHandler.deleteProperty(id, principal);
    return ResponseEntity.noContent().build();
  }

  private static String requestId(HttpServletRequest request) {
    Object value = request.getAttribute("requestId");
    return value == null ? "unknown" : value.toString();
  }
}
