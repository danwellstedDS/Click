package persistence;

import static org.assertj.core.api.Assertions.assertThat;

import domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import persistence.repository.UserRepositoryImpl;

@DataJpaTest
@Import({TestcontainersConfig.class, UserRepositoryImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryImplTest {
  @Autowired
  private UserRepositoryImpl repository;

  @Test
  void shouldReturnEmptyWhenUserNotFound() {
    assertThat(repository.findByEmail("nobody@example.com")).isEmpty();
  }

  @Test
  void shouldCreateAndFindByEmail() {
    User user = repository.create("alice@example.com", "hashedpassword");

    var found = repository.findByEmail("alice@example.com");
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(user.getId());
    assertThat(found.get().getEmail()).isEqualTo("alice@example.com");
    assertThat(found.get().getPasswordHash()).isEqualTo("hashedpassword");
  }

  @Test
  void shouldFindById() {
    User user = repository.create("bob@example.com", "hash123");
    var found = repository.findById(user.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(user.getId());
  }

  @Test
  void shouldReturnEmptyForUnknownId() {
    assertThat(repository.findById(java.util.UUID.randomUUID())).isEmpty();
  }
}
