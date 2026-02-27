package com.derbysoft.click.modules.identityaccess.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.derbysoft.click.modules.identityaccess.domain.OrgMembershipRepository;
import com.derbysoft.click.modules.identityaccess.domain.RefreshTokenRepository;
import com.derbysoft.click.modules.identityaccess.domain.UserRepository;
import com.derbysoft.click.modules.identityaccess.domain.aggregates.User;
import com.derbysoft.click.modules.identityaccess.domain.entities.OrgMembership;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.JwtService;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.LoginRequest;
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
  private OrgMembershipRepository orgMembershipRepository;

  @Mock
  private PropertyGroupQueryPort propertyGroupQueryPort;

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  @Mock
  private JwtService jwtService;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private AuthCommandHandler authCommandHandler;

  @Test
  void shouldReturnLoginResponseWhenCredentialsValid() {
    UUID userId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    UUID propertyGroupId = UUID.randomUUID();
    User user = User.create(userId, "user@example.com", "hash", "Demo User", true, Instant.now(), Instant.now());
    PropertyGroupInfo propertyGroup = new PropertyGroupInfo(propertyGroupId, "Demo Property Group", orgId);
    OrgMembership membership = new OrgMembership(UUID.randomUUID(), userId, orgId, true, Instant.now());

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("password"), any())).thenReturn(true);
    when(orgMembershipRepository.findByUserId(userId)).thenReturn(List.of(membership));
    when(propertyGroupQueryPort.findInfoByPrimaryOrgId(orgId)).thenReturn(Optional.of(propertyGroup));
    when(jwtService.createAccessToken(any(), anyLong())).thenReturn("jwt-token");

    var response = authCommandHandler.login(new LoginRequest("user@example.com", "password"));

    assertThat(response.token()).isEqualTo("jwt-token");
    assertThat(response.refreshToken()).isNotBlank();
    assertThat(response.user().email()).isEqualTo("user@example.com");
    assertThat(response.tenants()).hasSize(1);
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
}
