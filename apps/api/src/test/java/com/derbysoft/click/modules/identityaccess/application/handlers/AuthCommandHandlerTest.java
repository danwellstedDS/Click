package com.derbysoft.click.modules.identityaccess.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.identityaccess.domain.RefreshTokenRepository;
import com.derbysoft.click.modules.identityaccess.domain.TenantMembershipRepository;
import com.derbysoft.click.modules.identityaccess.domain.UserRepository;
import com.derbysoft.click.modules.identityaccess.domain.aggregates.User;
import com.derbysoft.click.modules.identityaccess.domain.entities.TenantMembership;
import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.JwtService;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.UserPrincipal;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.LoginRequest;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.TenantSummary;
import com.derbysoft.click.modules.organisationstructure.api.contracts.PropertyGroupInfo;
import com.derbysoft.click.modules.organisationstructure.api.ports.PropertyGroupQueryPort;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthCommandHandlerTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private TenantMembershipRepository tenantMembershipRepository;

  @Mock
  private PropertyGroupQueryPort propertyGroupQueryPort;

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  @Mock
  private JwtService jwtService;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private InProcessEventBus eventBus;

  @InjectMocks
  private AuthCommandHandler authCommandHandler;

  @Test
  void shouldBuildActorContextFromTenantMembership() {
    UUID userId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    User user = User.create(userId, "user@example.com", "hash", "Demo User", true, Instant.now(), Instant.now());
    TenantMembership membership = new TenantMembership(UUID.randomUUID(), userId, tenantId, Role.ADMIN, Instant.now());

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("password"), any())).thenReturn(true);
    when(tenantMembershipRepository.findByUserId(userId)).thenReturn(List.of(membership));
    when(jwtService.createAccessToken(any(), anyLong())).thenReturn("jwt-token");

    var response = authCommandHandler.login(new LoginRequest("user@example.com", "password"));

    assertThat(response.token()).isEqualTo("jwt-token");
    assertThat(response.refreshToken()).isNotBlank();
    assertThat(response.user().email()).isEqualTo("user@example.com");
    assertThat(response.tenants()).hasSize(1);
    assertThat(response.tenants().getFirst().tenantId()).isEqualTo(tenantId.toString());
    assertThat(response.tenants().getFirst().role()).isEqualTo(Role.ADMIN.name());
  }

  @Test
  void shouldPublishUserAuthenticatedEvent() {
    UUID userId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    User user = User.create(userId, "user@example.com", "hash", "Demo User", true, Instant.now(), Instant.now());
    TenantMembership membership = new TenantMembership(UUID.randomUUID(), userId, tenantId, Role.ADMIN, Instant.now());

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("password"), any())).thenReturn(true);
    when(tenantMembershipRepository.findByUserId(userId)).thenReturn(List.of(membership));
    when(jwtService.createAccessToken(any(), anyLong())).thenReturn("jwt-token");

    authCommandHandler.login(new LoginRequest("user@example.com", "password"));

    verify(eventBus).publish(any());
  }

  @Test
  void shouldThrowValidationWhenMissingEmailOrPassword() {
    assertThatThrownBy(() -> authCommandHandler.login(new LoginRequest(null, "")))
        .isInstanceOf(DomainError.ValidationError.class);
  }

  @Test
  void shouldThrowUnauthenticatedWhenPasswordInvalid() {
    UUID userId = UUID.randomUUID();
    User user = User.create(userId, "user@example.com", "hash", "Demo User", true, Instant.now(), Instant.now());

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("bad"), any())).thenReturn(false);

    assertThatThrownBy(() -> authCommandHandler.login(new LoginRequest("user@example.com", "bad")))
        .isInstanceOf(DomainError.Unauthenticated.class);
  }

  @Test
  void shouldThrowUnauthenticatedWhenNoTenantMemberships() {
    UUID userId = UUID.randomUUID();
    User user = User.create(userId, "user@example.com", "hash", "Demo User", true, Instant.now(), Instant.now());

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("password"), any())).thenReturn(true);
    when(tenantMembershipRepository.findByUserId(userId)).thenReturn(List.of());

    assertThatThrownBy(() -> authCommandHandler.login(new LoginRequest("user@example.com", "password")))
        .isInstanceOf(DomainError.Unauthenticated.class);
  }

  @Test
  void shouldReturnTenantSummaryListForCurrentUser() {
    UUID userId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    UserPrincipal principal = new UserPrincipal(userId, tenantId, "user@example.com", Role.ADMIN);
    TenantMembership membership = new TenantMembership(UUID.randomUUID(), userId, tenantId, Role.ADMIN, Instant.now());

    when(tenantMembershipRepository.findByUserId(userId)).thenReturn(List.of(membership));
    when(propertyGroupQueryPort.findInfoById(tenantId))
        .thenReturn(Optional.of(new PropertyGroupInfo(tenantId, "Marriott Hotels", null, "ACTIVE")));

    List<TenantSummary> result = authCommandHandler.listTenants(principal);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().tenantId()).isEqualTo(tenantId.toString());
    assertThat(result.getFirst().role()).isEqualTo(Role.ADMIN.name());
  }

  @Test
  void shouldResolveChainNameInTenantSummary() {
    UUID userId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    UserPrincipal principal = new UserPrincipal(userId, tenantId, "user@example.com", Role.ADMIN);
    TenantMembership membership = new TenantMembership(UUID.randomUUID(), userId, tenantId, Role.ADMIN, Instant.now());

    when(tenantMembershipRepository.findByUserId(userId)).thenReturn(List.of(membership));
    when(propertyGroupQueryPort.findInfoById(tenantId))
        .thenReturn(Optional.of(new PropertyGroupInfo(tenantId, "Hilton Worldwide", null, "ACTIVE")));

    List<TenantSummary> result = authCommandHandler.listTenants(principal);

    assertThat(result.getFirst().tenantName()).isEqualTo("Hilton Worldwide");
  }
}
