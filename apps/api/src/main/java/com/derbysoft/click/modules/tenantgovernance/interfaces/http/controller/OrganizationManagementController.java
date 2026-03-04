package com.derbysoft.click.modules.tenantgovernance.interfaces.http.controller;

import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.UserPrincipal;
import com.derbysoft.click.modules.tenantgovernance.domain.OrganizationRepository;
import com.derbysoft.click.modules.tenantgovernance.interfaces.http.dto.OrgResponse;
import com.derbysoft.click.sharedkernel.api.ApiResponse;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationManagementController {

  private final OrganizationRepository organizationRepository;

  public OrganizationManagementController(OrganizationRepository organizationRepository) {
    this.organizationRepository = organizationRepository;
  }

  @GetMapping
  public ApiResponse<List<OrgResponse>> listOrganizations(
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest request
  ) {
    if (principal.role() != Role.ADMIN) {
      throw new DomainError.Forbidden("AUTH_403", "Admin access required");
    }
    List<OrgResponse> orgs = organizationRepository.findAll().stream()
        .map(o -> new OrgResponse(o.getId(), o.getName(), o.getType().name()))
        .toList();
    return ApiResponse.success(orgs, requestId(request));
  }

  private static String requestId(HttpServletRequest request) {
    Object value = request.getAttribute("requestId");
    return value == null ? "unknown" : value.toString();
  }
}
