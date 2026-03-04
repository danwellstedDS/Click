package com.derbysoft.click.modules.organisationstructure.interfaces.http.controller;

import com.derbysoft.click.modules.identityaccess.infrastructure.security.UserPrincipal;
import com.derbysoft.click.modules.organisationstructure.application.handlers.ChainManagementService;
import com.derbysoft.click.modules.organisationstructure.domain.aggregates.PropertyGroup;
import com.derbysoft.click.modules.organisationstructure.interfaces.http.dto.ChainResponse;
import com.derbysoft.click.modules.organisationstructure.interfaces.http.dto.CreateChainRequest;
import com.derbysoft.click.modules.organisationstructure.interfaces.http.dto.UpdateChainStatusRequest;
import com.derbysoft.click.sharedkernel.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chains")
public class ChainManagementController {

  private final ChainManagementService chainManagementService;

  public ChainManagementController(ChainManagementService chainManagementService) {
    this.chainManagementService = chainManagementService;
  }

  @GetMapping
  public ApiResponse<List<ChainResponse>> listChains(
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request
  ) {
    List<ChainResponse> chains = chainManagementService.listChains(principal).stream()
        .map(ChainManagementController::toResponse)
        .toList();
    return ApiResponse.success(chains, requestId(request));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ChainResponse>> createChain(
      @RequestBody CreateChainRequest body,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request
  ) {
    PropertyGroup chain = chainManagementService.createChain(body.name(), body.timezone(), body.currency(), principal);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(toResponse(chain), requestId(request)));
  }

  @GetMapping("/{id}")
  public ApiResponse<ChainResponse> getChain(
      @PathVariable UUID id,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request
  ) {
    return ApiResponse.success(toResponse(chainManagementService.findById(id, principal)), requestId(request));
  }

  @PatchMapping("/{id}/status")
  public ApiResponse<ChainResponse> updateStatus(
      @PathVariable UUID id,
      @RequestBody UpdateChainStatusRequest body,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request
  ) {
    PropertyGroup chain = chainManagementService.updateStatus(id, body.status(), principal);
    return ApiResponse.success(toResponse(chain), requestId(request));
  }

  private static ChainResponse toResponse(PropertyGroup chain) {
    return new ChainResponse(
        chain.getId(),
        chain.getName(),
        chain.getStatus().name(),
        chain.getTimezone(),
        chain.getCurrency(),
        chain.getPrimaryOrgId(),
        chain.getCreatedAt(),
        chain.getUpdatedAt()
    );
  }

  private static String requestId(HttpServletRequest request) {
    Object value = request.getAttribute("requestId");
    return value == null ? "unknown" : value.toString();
  }
}
