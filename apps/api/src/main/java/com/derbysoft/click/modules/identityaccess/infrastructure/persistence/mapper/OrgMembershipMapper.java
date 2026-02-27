package com.derbysoft.click.modules.identityaccess.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.identityaccess.domain.entities.OrgMembership;
import com.derbysoft.click.modules.identityaccess.infrastructure.persistence.entity.OrgMembershipEntity;

public final class OrgMembershipMapper {
  private OrgMembershipMapper() {}

  public static OrgMembership toDomain(OrgMembershipEntity entity) {
    return new OrgMembership(
        entity.getId(),
        entity.getUserId(),
        entity.getOrganizationId(),
        entity.isOrgAdmin(),
        entity.getCreatedAt()
    );
  }
}
