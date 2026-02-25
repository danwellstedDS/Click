package domain.repository;

import domain.AccessScope;
import domain.ScopeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccessScopeRepository {
  List<AccessScope> findByPropertyGroupId(UUID propertyGroupId);
  Optional<AccessScope> findByPropertyGroupIdAndType(UUID propertyGroupId, ScopeType type);
  List<AccessScope> findByPropertyId(UUID propertyId);
  AccessScope create(ScopeType type, UUID propertyGroupId, UUID propertyId, UUID portfolioId);
}
