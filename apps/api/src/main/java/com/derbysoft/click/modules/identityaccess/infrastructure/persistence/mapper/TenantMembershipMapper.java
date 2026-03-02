package com.derbysoft.click.modules.identityaccess.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.identityaccess.domain.entities.TenantMembership;
import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;
import com.derbysoft.click.modules.identityaccess.infrastructure.persistence.entity.TenantMembershipEntity;

public final class TenantMembershipMapper {
  private TenantMembershipMapper() {}

  public static TenantMembership toDomain(TenantMembershipEntity entity) {
    return new TenantMembership(
        entity.getId(),
        entity.getUserId(),
        entity.getTenantId(),
        Role.valueOf(entity.getRole()),
        entity.getCreatedAt()
    );
  }
}
