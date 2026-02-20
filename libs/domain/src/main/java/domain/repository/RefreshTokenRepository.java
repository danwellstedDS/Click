package domain.repository;

import domain.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
  RefreshToken create(UUID userId, String tokenHash, Instant expiresAt);
  Optional<RefreshToken> findByTokenHash(String tokenHash);
  void deleteByTokenHash(String tokenHash);
  void deleteExpiredForUser(UUID userId);
}
