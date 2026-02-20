package domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthClaimsTest {
  @Test
  void shouldHoldClaimsData() {
    UUID userId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    AuthClaims claims = new AuthClaims(userId, tenantId, "user@example.com", Role.ADMIN);

    assertThat(claims.userId()).isEqualTo(userId);
    assertThat(claims.tenantId()).isEqualTo(tenantId);
    assertThat(claims.email()).isEqualTo("user@example.com");
    assertThat(claims.role()).isEqualTo(Role.ADMIN);
  }
}
