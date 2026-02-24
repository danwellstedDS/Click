package persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import persistence.entity.OrgMembershipEntity;

public interface OrgMembershipJpaRepository extends JpaRepository<OrgMembershipEntity, UUID> {
  List<OrgMembershipEntity> findByUserId(UUID userId);
  List<OrgMembershipEntity> findByOrganizationId(UUID organizationId);
  Optional<OrgMembershipEntity> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);
}
