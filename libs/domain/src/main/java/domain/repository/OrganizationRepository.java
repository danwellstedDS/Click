package domain.repository;

import domain.Organization;
import domain.OrganizationType;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository {
  Optional<Organization> findById(UUID id);
  Organization create(String name, OrganizationType type);
}
