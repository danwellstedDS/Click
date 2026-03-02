package com.derbysoft.click.modules.identityaccess.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.identityaccess.domain.TenantMembershipRepository;
import com.derbysoft.click.modules.identityaccess.domain.UserRepository;
import com.derbysoft.click.modules.identityaccess.domain.aggregates.User;
import com.derbysoft.click.modules.identityaccess.domain.entities.TenantMembership;
import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.UserPrincipal;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.CreateUserRequest;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.CreateUserResponse;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.UserDetailResponse;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.UserDetailResponse.MembershipInfo;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.UserListItemResponse;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagementHandler {

  private final UserRepository userRepository;
  private final TenantMembershipRepository tenantMembershipRepository;
  private final PasswordEncoder passwordEncoder;
  private final InProcessEventBus eventBus;

  public UserManagementHandler(
      UserRepository userRepository,
      TenantMembershipRepository tenantMembershipRepository,
      PasswordEncoder passwordEncoder,
      InProcessEventBus eventBus
  ) {
    this.userRepository = userRepository;
    this.tenantMembershipRepository = tenantMembershipRepository;
    this.passwordEncoder = passwordEncoder;
    this.eventBus = eventBus;
  }

  public List<UserListItemResponse> listUsers(UserPrincipal principal) {
    requireAdmin(principal);

    return tenantMembershipRepository.findByTenantId(principal.tenantId()).stream()
        .map(m -> {
          User user = userRepository.findById(m.userId())
              .orElseThrow(() -> new DomainError.NotFound("USR_002", "User not found"));
          return new UserListItemResponse(user.getId(), user.getEmail(), m.role().name(), user.getCreatedAt());
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
    UUID membershipId = UUID.randomUUID();
    TenantMembership membership = user.addMembership(membershipId, principal.tenantId(), request.role());
    tenantMembershipRepository.create(membershipId, user.getId(), principal.tenantId(), request.role());

    user.getEvents().forEach(event ->
        eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
    );
    user.clearEvents();

    UserListItemResponse userItem = new UserListItemResponse(
        user.getId(), user.getEmail(), membership.role().name(), user.getCreatedAt()
    );

    return new CreateUserResponse(userItem, tempPassword);
  }

  public UserDetailResponse getUser(UUID userId, UserPrincipal principal) {
    requireAdmin(principal);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new DomainError.NotFound("USR_002", "User not found"));

    TenantMembership tenantMembership = tenantMembershipRepository
        .findByUserAndTenant(userId, principal.tenantId())
        .orElseThrow(() -> new DomainError.NotFound("USR_002", "User not found"));

    List<MembershipInfo> membershipInfos = tenantMembershipRepository.findByUserId(userId).stream()
        .map(m -> new MembershipInfo(m.tenantId().toString(), m.role().name(), m.createdAt()))
        .toList();

    return new UserDetailResponse(
        user.getId(),
        user.getEmail(),
        tenantMembership.role().name(),
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

    tenantMembershipRepository.findByUserAndTenant(userId, principal.tenantId())
        .orElseThrow(() -> new DomainError.NotFound("USR_002", "User not found"));

    userRepository.deleteById(userId);
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
