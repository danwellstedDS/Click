package persistence.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import persistence.entity.RefreshTokenEntity;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {
  Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
  void deleteByTokenHash(String tokenHash);

  @Modifying
  @Query("delete from RefreshTokenEntity r where r.userId = :userId and r.expiresAt < :now")
  int deleteExpiredForUser(@Param("userId") UUID userId, @Param("now") Instant now);
}
