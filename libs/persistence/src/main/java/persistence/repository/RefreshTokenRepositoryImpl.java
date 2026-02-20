package persistence.repository;

import domain.RefreshToken;
import domain.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import persistence.entity.RefreshTokenEntity;
import persistence.mapper.RefreshTokenMapper;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {
  private final RefreshTokenJpaRepository refreshTokenJpaRepository;

  public RefreshTokenRepositoryImpl(RefreshTokenJpaRepository refreshTokenJpaRepository) {
    this.refreshTokenJpaRepository = refreshTokenJpaRepository;
  }

  @Override
  public RefreshToken create(UUID userId, String tokenHash, Instant expiresAt) {
    RefreshTokenEntity entity = new RefreshTokenEntity(userId, tokenHash, expiresAt);
    return RefreshTokenMapper.toDomain(refreshTokenJpaRepository.save(entity));
  }

  @Override
  public Optional<RefreshToken> findByTokenHash(String tokenHash) {
    return refreshTokenJpaRepository.findByTokenHash(tokenHash).map(RefreshTokenMapper::toDomain);
  }

  @Override
  @Transactional
  public void deleteByTokenHash(String tokenHash) {
    refreshTokenJpaRepository.deleteByTokenHash(tokenHash);
  }

  @Override
  @Transactional
  public void deleteExpiredForUser(UUID userId) {
    refreshTokenJpaRepository.deleteExpiredForUser(userId, Instant.now());
  }
}
