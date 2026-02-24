package persistence.mapper;

import domain.OrgMembership;
import persistence.entity.OrgMembershipEntity;

public final class OrgMembershipMapper {
  private OrgMembershipMapper() {
  }

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
