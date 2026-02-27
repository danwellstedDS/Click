package com.derbysoft.click.modules.organisationstructure.application.handlers;

import com.derbysoft.click.modules.identityaccess.infrastructure.security.UserPrincipal;
import com.derbysoft.click.modules.organisationstructure.domain.PropertyRepository;
import com.derbysoft.click.modules.organisationstructure.domain.entities.Property;
import com.derbysoft.click.modules.organisationstructure.interfaces.http.dto.CreatePropertyRequest;
import com.derbysoft.click.modules.organisationstructure.interfaces.http.dto.PropertyListItemResponse;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PropertyManagementHandler {

  private final PropertyRepository propertyRepository;

  public PropertyManagementHandler(PropertyRepository propertyRepository) {
    this.propertyRepository = propertyRepository;
  }

  public List<PropertyListItemResponse> listProperties(UserPrincipal principal) {
    requireAdmin(principal);
    return propertyRepository.findAllByPropertyGroupId(principal.tenantId()).stream()
        .filter(Property::isActive)
        .map(PropertyManagementHandler::toDto)
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
    if (principal.role() != com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role.ADMIN) {
      throw new DomainError.Forbidden("AUTH_403", "Admin access required");
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
