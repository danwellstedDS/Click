package com.derbysoft.click.modules.identityaccess.application.handlers;

import com.derbysoft.click.modules.identityaccess.domain.OrgMembershipRepository;
import com.derbysoft.click.modules.identityaccess.domain.RefreshTokenRepository;
import com.derbysoft.click.modules.identityaccess.domain.UserRepository;
import com.derbysoft.click.modules.identityaccess.domain.entities.OrgMembership;
import com.derbysoft.click.modules.identityaccess.domain.aggregates.User;
import com.derbysoft.click.modules.identityaccess.domain.valueobjects.AuthClaims;
import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.JwtService;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.UserPrincipal;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.LoginRequest;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.LoginResponse;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.MeResponse;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.TenantInfo;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.TokenResponse;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.UserInfo;
import com.derbysoft.click.modules.organisationstructure.api.contracts.PropertyGroupInfo;
import com.derbysoft.click.modules.organisationstructure.api.ports.PropertyGroupQueryPort;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
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
public class AuthCommandHandler {
  private static final long REFRESH_TOKEN_DAYS = 7L;
  private static final long ACCESS_TOKEN_SECONDS = 8L * 3600L;
  private static final String DUMMY_BCRYPT_HASH =
      "$2a$12$dummyhashfortimingtattackprevention000000000000000000000";

  private final UserRepository userRepository;
  private final OrgMembershipRepository orgMembershipRepository;
  private final PropertyGroupQueryPort propertyGroupQueryPort;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  public AuthCommandHandler(
      UserRepository userRepository,
      OrgMembershipRepository orgMembershipRepository,
      PropertyGroupQueryPort propertyGroupQueryPort,
      RefreshTokenRepository refreshTokenRepository,
      JwtService jwtService,
      PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.orgMembershipRepository = orgMembershipRepository;
    this.propertyGroupQueryPort = propertyGroupQueryPort;
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
      throw new DomainError.Unauthenticated("AUTH_001", "Invalid email or password");
    }

    User user = userOpt.get();
    List<OrgMembership> memberships = orgMembershipRepository.findByUserId(user.getId());
    if (memberships.isEmpty()) {
      throw new DomainError.Unauthenticated("AUTH_001", "No organization memberships found");
    }

    OrgMembership firstMembership = memberships.getFirst();
    PropertyGroupInfo propertyGroup = propertyGroupQueryPort.findInfoByPrimaryOrgId(firstMembership.organizationId())
        .orElseThrow(() -> new DomainError.Unauthenticated("AUTH_001", "No property group found for organization"));
    Role role = firstMembership.isOrgAdmin() ? Role.ADMIN : Role.VIEWER;

    AuthClaims claims = new AuthClaims(user.getId(), propertyGroup.id(), user.getEmail(), role);
    String accessToken = jwtService.createAccessToken(claims, ACCESS_TOKEN_SECONDS);

    String rawRefreshToken = UUID.randomUUID().toString();
    String refreshTokenHash = sha256(rawRefreshToken);
    Instant expiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_DAYS * 86400L);
    refreshTokenRepository.create(user.getId(), refreshTokenHash, expiresAt);

    List<TenantInfo> tenants = memberships.stream()
        .flatMap(m -> propertyGroupQueryPort.findInfoByPrimaryOrgId(m.organizationId()).stream()
            .map(pg -> new TenantInfo(pg.id().toString(), (m.isOrgAdmin() ? Role.ADMIN : Role.VIEWER).name())))
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
      throw new DomainError.Unauthenticated("AUTH_002", "Missing refresh token");
    }

    String tokenHash = sha256(rawToken);
    var storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
        .orElseThrow(() -> new DomainError.Unauthenticated("AUTH_002", "Invalid refresh token"));

    if (storedToken.getExpiresAt().isBefore(Instant.now())) {
      refreshTokenRepository.deleteByTokenHash(tokenHash);
      throw new DomainError.Unauthenticated("AUTH_003", "Refresh token expired");
    }

    User user = userRepository.findById(storedToken.getUserId())
        .orElseThrow(() -> new DomainError.Unauthenticated("AUTH_002", "User not found"));

    List<OrgMembership> memberships = orgMembershipRepository.findByUserId(user.getId());
    if (memberships.isEmpty()) {
      throw new DomainError.Unauthenticated("AUTH_002", "No organization memberships");
    }

    OrgMembership firstMembership = memberships.getFirst();
    PropertyGroupInfo propertyGroup = propertyGroupQueryPort.findInfoByPrimaryOrgId(firstMembership.organizationId())
        .orElseThrow(() -> new DomainError.Unauthenticated("AUTH_002", "No property group found for organization"));
    Role role = firstMembership.isOrgAdmin() ? Role.ADMIN : Role.VIEWER;

    AuthClaims claims = new AuthClaims(user.getId(), propertyGroup.id(), user.getEmail(), role);
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

    PropertyGroupInfo propertyGroup = propertyGroupQueryPort.findInfoById(propertyGroupId)
        .orElseThrow(() -> new DomainError.Forbidden("AUTH_403", "PropertyGroup not found"));

    OrgMembership membership = orgMembershipRepository
        .findByUserAndOrganization(principal.userId(), propertyGroup.primaryOrgId())
        .orElseThrow(() -> new DomainError.Forbidden("AUTH_403", "No access to requested property group"));

    Role role = membership.isOrgAdmin() ? Role.ADMIN : Role.VIEWER;
    AuthClaims newClaims = new AuthClaims(principal.userId(), propertyGroupId, principal.getUsername(), role);

    String newToken = jwtService.createAccessToken(newClaims, ACCESS_TOKEN_SECONDS);
    return new TokenResponse(newToken);
  }

  public MeResponse me(UserPrincipal principal) {
    String tenantName = propertyGroupQueryPort.findInfoById(principal.tenantId())
        .map(PropertyGroupInfo::name)
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
