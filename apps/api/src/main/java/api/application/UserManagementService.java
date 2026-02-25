package api.application;

import api.application.dto.CreateUserRequest;
import api.application.dto.CreateUserResponse;
import api.application.dto.UserDetailResponse;
import api.application.dto.UserDetailResponse.MembershipInfo;
import api.application.dto.UserListItemResponse;
import api.security.UserPrincipal;
import domain.OrgMembership;
import domain.PropertyGroup;
import domain.Role;
import domain.User;
import domain.error.DomainError;
import domain.repository.OrgMembershipRepository;
import domain.repository.PropertyGroupRepository;
import domain.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagementService {

  private final UserRepository userRepository;
  private final OrgMembershipRepository orgMembershipRepository;
  private final PropertyGroupRepository propertyGroupRepository;
  private final PasswordEncoder passwordEncoder;

  public UserManagementService(
      UserRepository userRepository,
      OrgMembershipRepository orgMembershipRepository,
      PropertyGroupRepository propertyGroupRepository,
      PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.orgMembershipRepository = orgMembershipRepository;
    this.propertyGroupRepository = propertyGroupRepository;
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
          String propertyGroupIdStr = propertyGroupRepository.findByPrimaryOrgId(m.organizationId())
              .map(pg -> pg.getId().toString())
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

    // org_memberships cascade on user delete
    userRepository.deleteById(userId);
  }

  private UUID resolveOrgId(UUID propertyGroupId) {
    PropertyGroup propertyGroup = propertyGroupRepository.findById(propertyGroupId)
        .orElseThrow(() -> new DomainError.NotFound("PGR_001", "PropertyGroup not found"));
    return propertyGroup.getPrimaryOrgId();
  }

  private static void requireAdmin(UserPrincipal principal) {
    if (principal.role() != Role.ADMIN) {
      throw new AuthException("AUTH_403", "Admin access required", 403);
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
