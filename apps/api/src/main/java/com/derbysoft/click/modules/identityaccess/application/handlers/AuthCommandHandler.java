package com.derbysoft.click.modules.identityaccess.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.identityaccess.domain.RefreshTokenRepository;
import com.derbysoft.click.modules.identityaccess.domain.TenantMembershipRepository;
import com.derbysoft.click.modules.identityaccess.domain.UserRepository;
import com.derbysoft.click.modules.identityaccess.domain.aggregates.User;
import com.derbysoft.click.modules.identityaccess.domain.entities.TenantMembership;
import com.derbysoft.click.modules.identityaccess.domain.events.UserAuthenticated;
import com.derbysoft.click.modules.identityaccess.domain.valueobjects.ActorContext;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.JwtService;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.UserPrincipal;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.LoginRequest;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.LoginResponse;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.MeResponse;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.TenantInfo;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.TenantSummary;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.TokenResponse;
import com.derbysoft.click.modules.identityaccess.interfaces.http.dto.UserInfo;
import com.derbysoft.click.modules.organisationstructure.api.contracts.PropertyGroupInfo;
import com.derbysoft.click.modules.organisationstructure.api.ports.PropertyGroupQueryPort;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
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
  private final TenantMembershipRepository tenantMembershipRepository;
  private final PropertyGroupQueryPort propertyGroupQueryPort;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;
  private final InProcessEventBus eventBus;

  public AuthCommandHandler(
      UserRepository userRepository,
      TenantMembershipRepository tenantMembershipRepository,
      PropertyGroupQueryPort propertyGroupQueryPort,
      RefreshTokenRepository refreshTokenRepository,
      JwtService jwtService,
      PasswordEncoder passwordEncoder,
      InProcessEventBus eventBus
  ) {
    this.userRepository = userRepository;
    this.tenantMembershipRepository = tenantMembershipRepository;
    this.propertyGroupQueryPort = propertyGroupQueryPort;
    this.refreshTokenRepository = refreshTokenRepository;
    this.jwtService = jwtService;
    this.passwordEncoder = passwordEncoder;
    this.eventBus = eventBus;
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
    List<TenantMembership> memberships = tenantMembershipRepository.findByUserId(user.getId());
    if (memberships.isEmpty()) {
      throw new DomainError.Unauthenticated("AUTH_001", "No tenant memberships found");
    }

    TenantMembership firstMembership = memberships.getFirst();
    ActorContext context = new ActorContext(user.getId(), firstMembership.tenantId(), user.getEmail(), firstMembership.role());
    String accessToken = jwtService.createAccessToken(context, ACCESS_TOKEN_SECONDS);

    String rawRefreshToken = UUID.randomUUID().toString();
    String refreshTokenHash = sha256(rawRefreshToken);
    Instant expiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_DAYS * 86400L);
    refreshTokenRepository.create(user.getId(), refreshTokenHash, expiresAt);

    eventBus.publish(EventEnvelope.of(
        UserAuthenticated.class.getSimpleName(),
        new UserAuthenticated(user.getId(), user.getEmail(), firstMembership.tenantId(), firstMembership.role(), Instant.now())
    ));

    List<TenantInfo> tenants = memberships.stream()
        .map(m -> new TenantInfo(m.tenantId().toString(), m.role().name()))
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

    List<TenantMembership> memberships = tenantMembershipRepository.findByUserId(user.getId());
    if (memberships.isEmpty()) {
      throw new DomainError.Unauthenticated("AUTH_002", "No tenant memberships");
    }

    TenantMembership firstMembership = memberships.getFirst();
    ActorContext context = new ActorContext(user.getId(), firstMembership.tenantId(), user.getEmail(), firstMembership.role());
    String newAccessToken = jwtService.createAccessToken(context, ACCESS_TOKEN_SECONDS);

    refreshTokenRepository.deleteByTokenHash(tokenHash);
    String newRawRefreshToken = UUID.randomUUID().toString();
    String newTokenHash = sha256(newRawRefreshToken);
    Instant newExpiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_DAYS * 86400L);
    refreshTokenRepository.create(user.getId(), newTokenHash, newExpiresAt);

    return new RefreshResult(new TokenResponse(newAccessToken), newRawRefreshToken);
  }

  public TokenResponse switchTenant(UserPrincipal principal, String tenantIdRaw) {
    UUID tenantId = parseTenantId(tenantIdRaw);

    propertyGroupQueryPort.findInfoById(tenantId)
        .orElseThrow(() -> new DomainError.Forbidden("AUTH_403", "PropertyGroup not found"));

    TenantMembership membership = tenantMembershipRepository
        .findByUserAndTenant(principal.userId(), tenantId)
        .orElseThrow(() -> new DomainError.Forbidden("AUTH_403", "No access to requested property group"));

    ActorContext newContext = new ActorContext(principal.userId(), tenantId, principal.getUsername(), membership.role());
    String newToken = jwtService.createAccessToken(newContext, ACCESS_TOKEN_SECONDS);
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

  public List<TenantSummary> listTenants(UserPrincipal principal) {
    return tenantMembershipRepository.findByUserId(principal.userId())
        .stream()
        .flatMap(m -> propertyGroupQueryPort.findInfoById(m.tenantId())
            .filter(info -> "ACTIVE".equals(info.status()))
            .map(info -> new TenantSummary(m.tenantId().toString(), info.name(), m.role().name()))
            .stream())
        .toList();
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
