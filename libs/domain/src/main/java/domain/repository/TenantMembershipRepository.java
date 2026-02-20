package domain.repository;

import domain.Role;
import domain.TenantMembership;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantMembershipRepository {
  List<TenantMembership> findByUserId(UUID userId);
  Optional<TenantMembership> findByUserAndTenant(UUID userId, UUID tenantId);
  TenantMembership create(UUID userId, UUID tenantId, Role role);
}
