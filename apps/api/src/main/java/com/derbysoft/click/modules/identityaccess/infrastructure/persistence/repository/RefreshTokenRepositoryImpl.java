package com.derbysoft.click.modules.identityaccess.infrastructure.persistence.repository;

import com.derbysoft.click.modules.identityaccess.domain.RefreshTokenRepository;
import com.derbysoft.click.modules.identityaccess.domain.entities.RefreshToken;
import com.derbysoft.click.modules.identityaccess.infrastructure.persistence.entity.RefreshTokenEntity;
import com.derbysoft.click.modules.identityaccess.infrastructure.persistence.mapper.RefreshTokenMapper;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
