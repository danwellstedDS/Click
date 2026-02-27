package com.derbysoft.click.modules.identityaccess.domain;

import com.derbysoft.click.modules.identityaccess.domain.entities.OrgMembership;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrgMembershipRepository {
  List<OrgMembership> findByUserId(UUID userId);
  List<OrgMembership> findByOrganizationId(UUID organizationId);
  Optional<OrgMembership> findByUserAndOrganization(UUID userId, UUID organizationId);
  OrgMembership create(UUID userId, UUID organizationId, boolean isOrgAdmin);
}
