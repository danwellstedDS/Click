package api.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import api.application.dto.LoginRequest;
import api.security.JwtService;
import domain.OrgMembership;
import domain.PropertyGroup;
import domain.User;
import domain.error.DomainError;
import domain.repository.OrgMembershipRepository;
import domain.repository.PropertyGroupRepository;
import domain.repository.RefreshTokenRepository;
import domain.repository.UserRepository;
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
class AuthServiceTest {
  @Mock
  private UserRepository userRepository;

  @Mock
  private OrgMembershipRepository orgMembershipRepository;

  @Mock
  private PropertyGroupRepository propertyGroupRepository;

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  @Mock
  private JwtService jwtService;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private AuthService authService;

  @Test
  void shouldReturnLoginResponseWhenCredentialsValid() {
    UUID userId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    UUID propertyGroupId = UUID.randomUUID();
    User user = User.create(userId, "user@example.com", "hash", "Demo User", true, Instant.now(), Instant.now());
    PropertyGroup propertyGroup = PropertyGroup.create(
        propertyGroupId, null, "Demo Property Group", "UTC", "USD", orgId, Instant.now(), Instant.now()
    );
    OrgMembership membership = new OrgMembership(UUID.randomUUID(), userId, orgId, true, Instant.now());

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("password"), any())).thenReturn(true);
    when(orgMembershipRepository.findByUserId(userId)).thenReturn(List.of(membership));
    when(propertyGroupRepository.findByPrimaryOrgId(orgId)).thenReturn(Optional.of(propertyGroup));
    when(jwtService.createAccessToken(any(), org.mockito.ArgumentMatchers.anyLong())).thenReturn("jwt-token");

    var response = authService.login(new LoginRequest("user@example.com", "password"));

    assertThat(response.token()).isEqualTo("jwt-token");
    assertThat(response.refreshToken()).isNotBlank();
    assertThat(response.user().email()).isEqualTo("user@example.com");
    assertThat(response.tenants()).hasSize(1);
  }

  @Test
  void shouldThrowValidationWhenMissingEmailOrPassword() {
    assertThatThrownBy(() -> authService.login(new LoginRequest(null, "")))
        .isInstanceOf(DomainError.ValidationError.class);
  }

  @Test
  void shouldThrowAuthExceptionWhenPasswordInvalid() {
    UUID userId = UUID.randomUUID();
    User user = User.create(userId, "user@example.com", "hash", "Demo User", true, Instant.now(), Instant.now());

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("bad"), any())).thenReturn(false);

    assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "bad")))
        .isInstanceOf(AuthException.class);
  }
}
