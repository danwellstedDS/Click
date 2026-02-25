package persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import persistence.entity.UserEntity;
import persistence.repository.RefreshTokenRepositoryImpl;

@DataJpaTest
@Import({TestcontainersConfig.class, RefreshTokenRepositoryImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RefreshTokenRepositoryImplTest {
  @Autowired
  private RefreshTokenRepositoryImpl repository;

  @Autowired
  private TestEntityManager entityManager;

  @Test
  void shouldCreateAndFindByTokenHash() {
    UUID userId = createUser("refresh@example.com");
    var token = repository.create(userId, "hash123", Instant.now().plusSeconds(3600));

    var found = repository.findByTokenHash("hash123");
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(token.getId());
  }

  @Test
  void shouldDeleteByTokenHash() {
    UUID userId = createUser("refresh2@example.com");
    repository.create(userId, "hash-delete", Instant.now().plusSeconds(3600));

    repository.deleteByTokenHash("hash-delete");

    assertThat(repository.findByTokenHash("hash-delete")).isEmpty();
  }

  @Test
  void shouldDeleteExpiredForUser() {
    UUID userId = createUser("refresh3@example.com");
    repository.create(userId, "expired", Instant.now().minusSeconds(60));
    repository.create(userId, "valid", Instant.now().plusSeconds(3600));

    repository.deleteExpiredForUser(userId);

    assertThat(repository.findByTokenHash("expired")).isEmpty();
    assertThat(repository.findByTokenHash("valid")).isPresent();
  }

  private UUID createUser(String email) {
    UserEntity entity = new UserEntity(email, "hash");
    return entityManager.persistAndFlush(entity).getId();
  }
}
