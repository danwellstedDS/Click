package persistence.mapper;

import domain.TenantMembership;
import persistence.entity.TenantMembershipEntity;

public final class TenantMembershipMapper {
  private TenantMembershipMapper() {
  }

  public static TenantMembership toDomain(TenantMembershipEntity entity) {
    return new TenantMembership(
        entity.getId(),
        entity.getUserId(),
        entity.getTenantId(),
        entity.getRole(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }
}
