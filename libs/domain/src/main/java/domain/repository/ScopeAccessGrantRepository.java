package domain.repository;

import domain.GrantRole;
import domain.ScopeAccessGrant;
import java.util.List;
import java.util.UUID;

public interface ScopeAccessGrantRepository {
  List<ScopeAccessGrant> findByOrganizationId(UUID organizationId);
  List<ScopeAccessGrant> findByScopeId(UUID scopeId);
  ScopeAccessGrant create(UUID organizationId, UUID scopeId, GrantRole role);
}
