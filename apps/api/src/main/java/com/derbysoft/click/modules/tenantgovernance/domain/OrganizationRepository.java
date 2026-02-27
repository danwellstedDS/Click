package com.derbysoft.click.modules.tenantgovernance.domain;

import com.derbysoft.click.modules.tenantgovernance.domain.aggregates.Organization;
import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.OrganizationType;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository {
  Optional<Organization> findById(UUID id);
  Organization create(String name, OrganizationType type);
}
