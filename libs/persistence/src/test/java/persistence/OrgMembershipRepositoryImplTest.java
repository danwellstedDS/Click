package persistence;

import static org.assertj.core.api.Assertions.assertThat;

import domain.OrgMembership;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import persistence.repository.OrgMembershipRepositoryImpl;

@DataJpaTest
@Import({TestcontainersConfig.class, OrgMembershipRepositoryImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OrgMembershipRepositoryImplTest {
  @Autowired
  private OrgMembershipRepositoryImpl repository;

  @Test
  void shouldCreateAndFindByUserId() {
    UUID userId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();

    OrgMembership membership = repository.create(userId, orgId, true);

    var memberships = repository.findByUserId(userId);
    assertThat(memberships).hasSize(1);
    assertThat(memberships.getFirst().id()).isEqualTo(membership.id());
    assertThat(memberships.getFirst().isOrgAdmin()).isTrue();
  }

  @Test
  void shouldFindByUserAndOrganization() {
    UUID userId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();

    repository.create(userId, orgId, false);

    var found = repository.findByUserAndOrganization(userId, orgId);
    assertThat(found).isPresent();
    assertThat(found.get().isOrgAdmin()).isFalse();
  }

  @Test
  void shouldReturnEmptyWhenNoMembership() {
    assertThat(repository.findByUserAndOrganization(UUID.randomUUID(), UUID.randomUUID())).isEmpty();
  }
}
