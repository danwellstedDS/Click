package com.derbysoft.click.modules.identityaccess.domain;

import com.derbysoft.click.modules.identityaccess.domain.entities.TenantMembership;
import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantMembershipRepository {
  List<TenantMembership> findByUserId(UUID userId);
  List<TenantMembership> findByTenantId(UUID tenantId);
  Optional<TenantMembership> findByUserAndTenant(UUID userId, UUID tenantId);
  TenantMembership create(UUID membershipId, UUID userId, UUID tenantId, Role role);
}
