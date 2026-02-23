package api.application;

import api.application.dto.CreateUserRequest;
import api.application.dto.CreateUserResponse;
import api.application.dto.UserDetailResponse;
import api.application.dto.UserDetailResponse.MembershipInfo;
import api.application.dto.UserListItemResponse;
import api.security.UserPrincipal;
import domain.Role;
import domain.TenantMembership;
import domain.User;
import domain.error.DomainError;
import domain.repository.TenantMembershipRepository;
import domain.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagementService {

  private final UserRepository userRepository;
  private final TenantMembershipRepository membershipRepository;
  private final PasswordEncoder passwordEncoder;

  public UserManagementService(
      UserRepository userRepository,
      TenantMembershipRepository membershipRepository,
      PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.membershipRepository = membershipRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public List<UserListItemResponse> listUsers(UserPrincipal principal) {
    requireAdmin(principal);
    UUID tenantId = principal.tenantId();

    List<TenantMembership> memberships = membershipRepository.findAllByTenantId(tenantId);
    List<UUID> userIds = memberships.stream().map(TenantMembership::userId).toList();
    List<User> users = userRepository.findAllByTenantId(tenantId);

    Map<UUID, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
    Map<UUID, TenantMembership> membershipByUserId = memberships.stream()
        .collect(Collectors.toMap(TenantMembership::userId, m -> m));

    return userIds.stream()
        .filter(userMap::containsKey)
        .map(id -> {
          User user = userMap.get(id);
          TenantMembership membership = membershipByUserId.get(id);
          return new UserListItemResponse(
              user.getId(),
              user.getEmail(),
              membership.role().name(),
              user.getCreatedAt()
          );
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

    String tempPassword = UUID.randomUUID().toString();
    String hashedPassword = passwordEncoder.encode(tempPassword);

    User user = userRepository.create(request.email(), hashedPassword);
    TenantMembership membership = membershipRepository.create(
        user.getId(), principal.tenantId(), request.role()
    );

    UserListItemResponse userItem = new UserListItemResponse(
        user.getId(),
        user.getEmail(),
        membership.role().name(),
        user.getCreatedAt()
    );

    return new CreateUserResponse(userItem, tempPassword);
  }

  public UserDetailResponse getUser(UUID userId, UserPrincipal principal) {
    requireAdmin(principal);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new DomainError.NotFound("USR_002", "User not found"));

    List<TenantMembership> memberships = membershipRepository.findByUserId(userId);

    boolean belongsToTenant = memberships.stream()
        .anyMatch(m -> m.tenantId().equals(principal.tenantId()));

    if (!belongsToTenant) {
      throw new DomainError.NotFound("USR_002", "User not found");
    }

    String roleInTenant = memberships.stream()
        .filter(m -> m.tenantId().equals(principal.tenantId()))
        .map(m -> m.role().name())
        .findFirst()
        .orElse(Role.VIEWER.name());

    List<MembershipInfo> membershipInfos = memberships.stream()
        .map(m -> new MembershipInfo(m.tenantId().toString(), m.role().name(), m.createdAt()))
        .toList();

    return new UserDetailResponse(
        user.getId(),
        user.getEmail(),
        roleInTenant,
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

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new DomainError.NotFound("USR_002", "User not found"));

    List<TenantMembership> memberships = membershipRepository.findByUserId(user.getId());
    boolean belongsToTenant = memberships.stream()
        .anyMatch(m -> m.tenantId().equals(principal.tenantId()));

    if (!belongsToTenant) {
      throw new DomainError.NotFound("USR_002", "User not found");
    }

    membershipRepository.deleteByUserId(userId);
    userRepository.deleteById(userId);
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
