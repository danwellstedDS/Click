package api.application;

import api.application.dto.LoginRequest;
import api.application.dto.LoginResponse;
import api.application.dto.MeResponse;
import api.application.dto.TenantInfo;
import api.application.dto.TokenResponse;
import api.application.dto.UserInfo;
import api.security.JwtService;
import api.security.UserPrincipal;
import domain.AuthClaims;
import domain.Role;
import domain.TenantMembership;
import domain.User;
import domain.error.DomainError;
import domain.repository.RefreshTokenRepository;
import domain.repository.TenantMembershipRepository;
import domain.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private static final long REFRESH_TOKEN_DAYS = 7L;
  private static final long ACCESS_TOKEN_SECONDS = 8L * 3600L;
  private static final String DUMMY_BCRYPT_HASH =
      "$2a$12$dummyhashfortimingtattackprevention000000000000000000000";

  private final UserRepository userRepository;
  private final TenantMembershipRepository membershipRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  public AuthService(
      UserRepository userRepository,
      TenantMembershipRepository membershipRepository,
      RefreshTokenRepository refreshTokenRepository,
      JwtService jwtService,
      PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.membershipRepository = membershipRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.jwtService = jwtService;
    this.passwordEncoder = passwordEncoder;
  }

  public LoginResponse login(LoginRequest request) {
    if (request == null || isBlank(request.email()) || isBlank(request.password())) {
      throw new DomainError.ValidationError("VAL_001", "email and password are required");
    }

    Optional<User> userOpt = userRepository.findByEmail(request.email());
    String hashToVerify = userOpt.map(User::getPasswordHash).orElse(DUMMY_BCRYPT_HASH);
    boolean passwordValid = passwordEncoder.matches(request.password(), hashToVerify);

    if (userOpt.isEmpty() || !passwordValid) {
      throw new AuthException("AUTH_001", "Invalid email or password", 401);
    }

    User user = userOpt.get();
    List<TenantMembership> memberships = membershipRepository.findByUserId(user.getId());
    if (memberships.isEmpty()) {
      throw new AuthException("AUTH_001", "No tenant memberships found", 401);
    }

    TenantMembership firstMembership = memberships.getFirst();
    AuthClaims claims = new AuthClaims(user.getId(), firstMembership.tenantId(), user.getEmail(), firstMembership.role());
    String accessToken = jwtService.createAccessToken(claims, ACCESS_TOKEN_SECONDS);

    String rawRefreshToken = UUID.randomUUID().toString();
    String refreshTokenHash = sha256(rawRefreshToken);
    Instant expiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_DAYS * 86400L);
    refreshTokenRepository.create(user.getId(), refreshTokenHash, expiresAt);

    List<TenantInfo> tenants = memberships.stream()
        .map(membership -> new TenantInfo(membership.tenantId().toString(), membership.role().name()))
        .toList();

    return new LoginResponse(
        accessToken,
        rawRefreshToken,
        new UserInfo(user.getId().toString(), user.getEmail()),
        tenants
    );
  }

  public RefreshResult refresh(String rawToken) {
    if (isBlank(rawToken)) {
      throw new AuthException("AUTH_002", "Missing refresh token", 401);
    }

    String tokenHash = sha256(rawToken);
    var storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
        .orElseThrow(() -> new AuthException("AUTH_002", "Invalid refresh token", 401));

    if (storedToken.getExpiresAt().isBefore(Instant.now())) {
      refreshTokenRepository.deleteByTokenHash(tokenHash);
      throw new AuthException("AUTH_003", "Refresh token expired", 401);
    }

    User user = userRepository.findById(storedToken.getUserId())
        .orElseThrow(() -> new AuthException("AUTH_002", "User not found", 401));

    List<TenantMembership> memberships = membershipRepository.findByUserId(user.getId());
    if (memberships.isEmpty()) {
      throw new AuthException("AUTH_002", "No tenant memberships", 401);
    }

    TenantMembership firstMembership = memberships.getFirst();
    AuthClaims claims = new AuthClaims(user.getId(), firstMembership.tenantId(), user.getEmail(), firstMembership.role());
    String newAccessToken = jwtService.createAccessToken(claims, ACCESS_TOKEN_SECONDS);

    refreshTokenRepository.deleteByTokenHash(tokenHash);
    String newRawRefreshToken = UUID.randomUUID().toString();
    String newTokenHash = sha256(newRawRefreshToken);
    Instant newExpiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_DAYS * 86400L);
    refreshTokenRepository.create(user.getId(), newTokenHash, newExpiresAt);

    return new RefreshResult(new TokenResponse(newAccessToken), newRawRefreshToken);
  }

  public TokenResponse switchTenant(UserPrincipal principal, String tenantIdRaw) {
    UUID tenantId = parseTenantId(tenantIdRaw);

    TenantMembership membership = membershipRepository
        .findByUserAndTenant(principal.userId(), tenantId)
        .orElseThrow(() -> new AuthException("AUTH_403", "No membership for requested tenant", 403));

    AuthClaims newClaims = new AuthClaims(
        principal.userId(),
        tenantId,
        principal.getUsername(),
        membership.role()
    );

    String newToken = jwtService.createAccessToken(newClaims, ACCESS_TOKEN_SECONDS);
    return new TokenResponse(newToken);
  }

  public MeResponse me(UserPrincipal principal) {
    return new MeResponse(
        principal.userId().toString(),
        principal.getUsername(),
        principal.tenantId().toString(),
        principal.role().name()
    );
  }

  public void logout(UserPrincipal principal, String refreshToken) {
    if (!isBlank(refreshToken)) {
      refreshTokenRepository.deleteByTokenHash(sha256(refreshToken));
    }
    refreshTokenRepository.deleteExpiredForUser(principal.userId());
  }

  private static UUID parseTenantId(String tenantIdRaw) {
    if (isBlank(tenantIdRaw)) {
      throw new DomainError.ValidationError("VAL_001", "tenantId is required");
    }
    try {
      return UUID.fromString(tenantIdRaw);
    } catch (IllegalArgumentException ex) {
      throw new DomainError.ValidationError("VAL_001", "tenantId is required");
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static String sha256(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hashed);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  public record RefreshResult(TokenResponse response, String refreshToken) {}
}
