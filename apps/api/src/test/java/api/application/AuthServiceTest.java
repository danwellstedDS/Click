package api.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import api.application.dto.LoginRequest;
import api.security.JwtService;
import domain.Role;
import domain.TenantMembership;
import domain.User;
import domain.error.DomainError;
import domain.repository.RefreshTokenRepository;
import domain.repository.TenantMembershipRepository;
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
  private TenantMembershipRepository membershipRepository;

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
    UUID tenantId = UUID.randomUUID();
    User user = User.create(userId, "user@example.com", "hash", Instant.now(), Instant.now());

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("password"), any())).thenReturn(true);
    when(membershipRepository.findByUserId(userId))
        .thenReturn(List.of(new TenantMembership(UUID.randomUUID(), userId, tenantId, Role.ADMIN, Instant.now(), Instant.now())));
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
    User user = User.create(userId, "user@example.com", "hash", Instant.now(), Instant.now());

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("bad"), any())).thenReturn(false);

    assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "bad")))
        .isInstanceOf(AuthException.class);
  }
}
