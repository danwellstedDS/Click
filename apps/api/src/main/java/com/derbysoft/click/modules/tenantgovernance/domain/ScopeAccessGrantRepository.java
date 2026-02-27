package com.derbysoft.click.modules.tenantgovernance.domain;

import com.derbysoft.click.modules.tenantgovernance.domain.entities.ScopeAccessGrant;
import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.GrantRole;
import java.util.List;
import java.util.UUID;

public interface ScopeAccessGrantRepository {
  List<ScopeAccessGrant> findByOrganizationId(UUID organizationId);
  List<ScopeAccessGrant> findByScopeId(UUID scopeId);
  ScopeAccessGrant create(UUID organizationId, UUID scopeId, GrantRole role);
}
