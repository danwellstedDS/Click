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
import domain.OrgMembership;
import domain.PropertyGroup;
import domain.Role;
import domain.User;
import domain.error.DomainError;
import domain.repository.OrgMembershipRepository;
import domain.repository.PropertyGroupRepository;
import domain.repository.RefreshTokenRepository;
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
  private final OrgMembershipRepository orgMembershipRepository;
  private final PropertyGroupRepository propertyGroupRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  public AuthService(
      UserRepository userRepository,
      OrgMembershipRepository orgMembershipRepository,
      PropertyGroupRepository propertyGroupRepository,
      RefreshTokenRepository refreshTokenRepository,
      JwtService jwtService,
      PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.orgMembershipRepository = orgMembershipRepository;
    this.propertyGroupRepository = propertyGroupRepository;
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
    List<OrgMembership> memberships = orgMembershipRepository.findByUserId(user.getId());
    if (memberships.isEmpty()) {
      throw new AuthException("AUTH_001", "No organization memberships found", 401);
    }

    OrgMembership firstMembership = memberships.getFirst();
    PropertyGroup propertyGroup = propertyGroupRepository.findByPrimaryOrgId(firstMembership.organizationId())
        .orElseThrow(() -> new AuthException("AUTH_001", "No property group found for organization", 401));
    Role role = firstMembership.isOrgAdmin() ? Role.ADMIN : Role.VIEWER;

    AuthClaims claims = new AuthClaims(user.getId(), propertyGroup.getId(), user.getEmail(), role);
    String accessToken = jwtService.createAccessToken(claims, ACCESS_TOKEN_SECONDS);

    String rawRefreshToken = UUID.randomUUID().toString();
    String refreshTokenHash = sha256(rawRefreshToken);
    Instant expiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_DAYS * 86400L);
    refreshTokenRepository.create(user.getId(), refreshTokenHash, expiresAt);

    List<TenantInfo> tenants = memberships.stream()
        .flatMap(m -> propertyGroupRepository.findByPrimaryOrgId(m.organizationId()).stream()
            .map(pg -> new TenantInfo(pg.getId().toString(), (m.isOrgAdmin() ? Role.ADMIN : Role.VIEWER).name())))
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

    List<OrgMembership> memberships = orgMembershipRepository.findByUserId(user.getId());
    if (memberships.isEmpty()) {
      throw new AuthException("AUTH_002", "No organization memberships", 401);
    }

    OrgMembership firstMembership = memberships.getFirst();
    PropertyGroup propertyGroup = propertyGroupRepository.findByPrimaryOrgId(firstMembership.organizationId())
        .orElseThrow(() -> new AuthException("AUTH_002", "No property group found for organization", 401));
    Role role = firstMembership.isOrgAdmin() ? Role.ADMIN : Role.VIEWER;

    AuthClaims claims = new AuthClaims(user.getId(), propertyGroup.getId(), user.getEmail(), role);
    String newAccessToken = jwtService.createAccessToken(claims, ACCESS_TOKEN_SECONDS);

    refreshTokenRepository.deleteByTokenHash(tokenHash);
    String newRawRefreshToken = UUID.randomUUID().toString();
    String newTokenHash = sha256(newRawRefreshToken);
    Instant newExpiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_DAYS * 86400L);
    refreshTokenRepository.create(user.getId(), newTokenHash, newExpiresAt);

    return new RefreshResult(new TokenResponse(newAccessToken), newRawRefreshToken);
  }

  public TokenResponse switchTenant(UserPrincipal principal, String tenantIdRaw) {
    UUID propertyGroupId = parseTenantId(tenantIdRaw);

    PropertyGroup propertyGroup = propertyGroupRepository.findById(propertyGroupId)
        .orElseThrow(() -> new AuthException("AUTH_403", "PropertyGroup not found", 403));

    OrgMembership membership = orgMembershipRepository
        .findByUserAndOrganization(principal.userId(), propertyGroup.getPrimaryOrgId())
        .orElseThrow(() -> new AuthException("AUTH_403", "No access to requested property group", 403));

    Role role = membership.isOrgAdmin() ? Role.ADMIN : Role.VIEWER;
    AuthClaims newClaims = new AuthClaims(principal.userId(), propertyGroupId, principal.getUsername(), role);

    String newToken = jwtService.createAccessToken(newClaims, ACCESS_TOKEN_SECONDS);
    return new TokenResponse(newToken);
  }

  public MeResponse me(UserPrincipal principal) {
    String tenantName = propertyGroupRepository.findById(principal.tenantId())
        .map(PropertyGroup::getName)
        .orElse(principal.tenantId().toString());
    return new MeResponse(
        principal.userId().toString(),
        principal.getUsername(),
        principal.tenantId().toString(),
        tenantName,
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
