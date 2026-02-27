package com.derbysoft.click.modules.identityaccess.application.handlers;

import com.derbysoft.click.modules.identityaccess.domain.OrgMembershipRepository;
import com.derbysoft.click.modules.identityaccess.domain.UserRepository;
import com.derbysoft.click.modules.identityaccess.domain.entities.OrgMembership;
import com.derbysoft.click.modules.identityaccess.domain.aggregates.User;
import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.UserPrincipal;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.CreateUserRequest;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.CreateUserResponse;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.UserDetailResponse;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.UserDetailResponse.MembershipInfo;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.UserListItemResponse;
import com.derbysoft.click.modules.organisationstructure.api.contracts.PropertyGroupInfo;
import com.derbysoft.click.modules.organisationstructure.api.ports.PropertyGroupQueryPort;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagementHandler {

  private final UserRepository userRepository;
  private final OrgMembershipRepository orgMembershipRepository;
  private final PropertyGroupQueryPort propertyGroupQueryPort;
  private final PasswordEncoder passwordEncoder;

  public UserManagementHandler(
      UserRepository userRepository,
      OrgMembershipRepository orgMembershipRepository,
      PropertyGroupQueryPort propertyGroupQueryPort,
      PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.orgMembershipRepository = orgMembershipRepository;
    this.propertyGroupQueryPort = propertyGroupQueryPort;
    this.passwordEncoder = passwordEncoder;
  }

  public List<UserListItemResponse> listUsers(UserPrincipal principal) {
    requireAdmin(principal);
    UUID orgId = resolveOrgId(principal.tenantId());

    return orgMembershipRepository.findByOrganizationId(orgId).stream()
        .map(m -> {
          User user = userRepository.findById(m.userId())
              .orElseThrow(() -> new DomainError.NotFound("USR_002", "User not found"));
          String role = m.isOrgAdmin() ? Role.ADMIN.name() : Role.VIEWER.name();
          return new UserListItemResponse(user.getId(), user.getEmail(), role, user.getCreatedAt());
        })
        .toList();
  }

  @Transactional
  public CreateUserResponse createUser(CreateUserRequest request, UserPrincipal principal) {
    requireAdmin(principal);

    if (request == null || isBlank(request.email())) {
      throw new DomainError.ValidationError("VAL_001", "email is required");
    }
    if (request.role() == null) {
      throw new DomainError.ValidationError("VAL_001", "role is required");
    }

    userRepository.findByEmail(request.email()).ifPresent(existing -> {
      throw new DomainError.Conflict("USR_001", "A user with this email already exists");
    });

    UUID orgId = resolveOrgId(principal.tenantId());

    String tempPassword = UUID.randomUUID().toString();
    String hashedPassword = passwordEncoder.encode(tempPassword);

    User user = userRepository.create(request.email(), hashedPassword);
    OrgMembership membership = orgMembershipRepository.create(
        user.getId(), orgId, request.role() == Role.ADMIN
    );

    String role = membership.isOrgAdmin() ? Role.ADMIN.name() : Role.VIEWER.name();
    UserListItemResponse userItem = new UserListItemResponse(
        user.getId(), user.getEmail(), role, user.getCreatedAt()
    );

    return new CreateUserResponse(userItem, tempPassword);
  }

  public UserDetailResponse getUser(UUID userId, UserPrincipal principal) {
    requireAdmin(principal);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new DomainError.NotFound("USR_002", "User not found"));

    UUID orgId = resolveOrgId(principal.tenantId());

    OrgMembership orgMembership = orgMembershipRepository.findByUserAndOrganization(userId, orgId)
        .orElseThrow(() -> new DomainError.NotFound("USR_002", "User not found"));

    String roleInPropertyGroup = orgMembership.isOrgAdmin() ? Role.ADMIN.name() : Role.VIEWER.name();

    List<MembershipInfo> membershipInfos = orgMembershipRepository.findByUserId(userId).stream()
        .map(m -> {
          String propertyGroupIdStr = propertyGroupQueryPort.findInfoByPrimaryOrgId(m.organizationId())
              .map(pg -> pg.id().toString())
              .orElse(m.organizationId().toString());
          String role = m.isOrgAdmin() ? Role.ADMIN.name() : Role.VIEWER.name();
          return new MembershipInfo(propertyGroupIdStr, role, m.createdAt());
        })
        .toList();

    return new UserDetailResponse(
        user.getId(),
        user.getEmail(),
        roleInPropertyGroup,
        user.getCreatedAt(),
        user.getUpdatedAt(),
        membershipInfos
    );
  }

  @Transactional
  public void deleteUser(UUID userId, UserPrincipal principal) {
    requireAdmin(principal);

    if (userId.equals(principal.userId())) {
      throw new DomainError.ValidationError("USR_003", "You cannot delete your own account");
    }

    userRepository.findById(userId)
        .orElseThrow(() -> new DomainError.NotFound("USR_002", "User not found"));

    UUID orgId = resolveOrgId(principal.tenantId());
    orgMembershipRepository.findByUserAndOrganization(userId, orgId)
        .orElseThrow(() -> new DomainError.NotFound("USR_002", "User not found"));

    userRepository.deleteById(userId);
  }

  private UUID resolveOrgId(UUID propertyGroupId) {
    return propertyGroupQueryPort.findInfoById(propertyGroupId)
        .map(PropertyGroupInfo::primaryOrgId)
        .orElseThrow(() -> new DomainError.NotFound("PGR_001", "PropertyGroup not found"));
  }

  private static void requireAdmin(UserPrincipal principal) {
    if (principal.role() != Role.ADMIN) {
      throw new DomainError.Forbidden("AUTH_403", "Admin access required");
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
