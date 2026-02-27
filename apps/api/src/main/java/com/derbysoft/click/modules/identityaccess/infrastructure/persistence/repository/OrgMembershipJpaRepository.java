package com.derbysoft.click.modules.identityaccess.infrastructure.persistence.repository;

import com.derbysoft.click.modules.identityaccess.infrastructure.persistence.entity.OrgMembershipEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrgMembershipJpaRepository extends JpaRepository<OrgMembershipEntity, UUID> {
  List<OrgMembershipEntity> findByUserId(UUID userId);
  List<OrgMembershipEntity> findByOrganizationId(UUID organizationId);
  Optional<OrgMembershipEntity> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);
}
