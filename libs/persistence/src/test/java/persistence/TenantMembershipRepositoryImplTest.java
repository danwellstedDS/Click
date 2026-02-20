package persistence;

import static org.assertj.core.api.Assertions.assertThat;

import domain.Role;
import domain.TenantMembership;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import persistence.entity.TenantEntity;
import persistence.entity.UserEntity;
import persistence.repository.TenantMembershipRepositoryImpl;

@DataJpaTest
@Import({TestcontainersConfig.class, TenantMembershipRepositoryImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TenantMembershipRepositoryImplTest {
  @Autowired
  private TenantMembershipRepositoryImpl repository;

  @Autowired
  private TestEntityManager entityManager;

  @Test
  void shouldCreateAndFindByUserId() {
    UUID userId = createUser("member@example.com");
    UUID tenantId = createTenant("Tenant One");

    TenantMembership membership = repository.create(userId, tenantId, Role.ADMIN);

    var memberships = repository.findByUserId(userId);
    assertThat(memberships).hasSize(1);
    assertThat(memberships.getFirst().id()).isEqualTo(membership.id());
  }

  @Test
  void shouldFindByUserAndTenant() {
    UUID userId = createUser("member2@example.com");
    UUID tenantId = createTenant("Tenant Two");

    repository.create(userId, tenantId, Role.VIEWER);

    var found = repository.findByUserAndTenant(userId, tenantId);
    assertThat(found).isPresent();
    assertThat(found.get().role()).isEqualTo(Role.VIEWER);
  }

  @Test
  void shouldReturnEmptyWhenNoMembership() {
    UUID userId = createUser("nomember@example.com");
    UUID tenantId = createTenant("Tenant Three");

    assertThat(repository.findByUserAndTenant(userId, tenantId)).isEmpty();
  }

  private UUID createUser(String email) {
    UserEntity entity = new UserEntity(email, "hash");
    return entityManager.persistAndFlush(entity).getId();
  }

  private UUID createTenant(String name) {
    TenantEntity entity = new TenantEntity(name);
    return entityManager.persistAndFlush(entity).getId();
  }
}
