package api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import domain.AuthClaims;
import domain.Role;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private static final String ISSUER = "click5";
  private static final String AUDIENCE = "click5-api";

  private final Algorithm algorithm;
  private final JWTVerifier verifier;

  public JwtService(@Value("${jwt.secret}") String secret) {
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("JWT_SECRET environment variable is required");
    }
    this.algorithm = Algorithm.HMAC256(secret);
    this.verifier = JWT.require(algorithm)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build();
  }

  public String createAccessToken(AuthClaims claims, long expiresInSeconds) {
    Instant now = Instant.now();
    return JWT.create()
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .withSubject(claims.userId().toString())
        .withClaim("tenantId", claims.tenantId().toString())
        .withClaim("email", claims.email())
        .withClaim("role", claims.role().name())
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(now.plusSeconds(expiresInSeconds)))
        .sign(algorithm);
  }

  public AuthClaims verifyAndExtract(String token) throws JWTVerificationException {
    DecodedJWT decoded = verifier.verify(token);
    return extractClaims(decoded);
  }

  private AuthClaims extractClaims(DecodedJWT decoded) {
    UUID userId = UUID.fromString(decoded.getSubject());
    UUID tenantId = UUID.fromString(decoded.getClaim("tenantId").asString());
    String email = decoded.getClaim("email").asString();
    Role role = Role.valueOf(decoded.getClaim("role").asString());
    return new AuthClaims(userId, tenantId, email, role);
  }
}
