package api.application;

import api.application.dto.CreatePropertyRequest;
import api.application.dto.PropertyListItemResponse;
import api.security.UserPrincipal;
import domain.Property;
import domain.Role;
import domain.error.DomainError;
import domain.repository.PropertyRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PropertyManagementService {

  private final PropertyRepository propertyRepository;

  public PropertyManagementService(PropertyRepository propertyRepository) {
    this.propertyRepository = propertyRepository;
  }

  public List<PropertyListItemResponse> listProperties(UserPrincipal principal) {
    requireAdmin(principal);
    return propertyRepository.findAllByPropertyGroupId(principal.tenantId()).stream()
        .filter(Property::isActive)
        .map(PropertyManagementService::toDto)
        .toList();
  }

  @Transactional
  public PropertyListItemResponse createProperty(CreatePropertyRequest request, UserPrincipal principal) {
    requireAdmin(principal);

    if (request == null || isBlank(request.name())) {
      throw new DomainError.ValidationError("VAL_001", "name is required");
    }

    Property property = propertyRepository.create(
        principal.tenantId(),
        request.name(),
        request.isActive(),
        request.externalPropertyRef()
    );
    return toDto(property);
  }

  @Transactional
  public void deleteProperty(UUID id, UserPrincipal principal) {
    requireAdmin(principal);
    propertyRepository.deactivate(id);
  }

  private static PropertyListItemResponse toDto(Property property) {
    return new PropertyListItemResponse(
        property.getId(),
        property.getName(),
        property.isActive(),
        property.getExternalPropertyRef(),
        property.getCreatedAt()
    );
  }

  private static void requireAdmin(UserPrincipal principal) {
    if (principal.role() != Role.ADMIN) {
      throw new AuthException("AUTH_403", "Admin access required", 403);
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
