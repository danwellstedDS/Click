package api.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import api.application.dto.CreatePropertyRequest;
import api.application.dto.PropertyListItemResponse;
import api.security.UserPrincipal;
import domain.Property;
import domain.Role;
import domain.repository.PropertyRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PropertyManagementServiceTest {

  @Mock
  private PropertyRepository propertyRepository;

  @InjectMocks
  private PropertyManagementService propertyManagementService;

  private static final UUID TENANT_ID = UUID.randomUUID();
  private static final UUID PROPERTY_ID = UUID.randomUUID();

  private static UserPrincipal adminPrincipal() {
    return new UserPrincipal(UUID.randomUUID(), TENANT_ID, "admin@example.com", Role.ADMIN);
  }

  private static UserPrincipal viewerPrincipal() {
    return new UserPrincipal(UUID.randomUUID(), TENANT_ID, "viewer@example.com", Role.VIEWER);
  }

  private static Property activeProperty(UUID id) {
    return Property.create(id, TENANT_ID, "Test Property", true, null, Instant.now(), Instant.now());
  }

  private static Property inactiveProperty(UUID id) {
    return Property.create(id, TENANT_ID, "Inactive Property", false, null, Instant.now(), Instant.now());
  }

  @Test
  void listProperties_returnsOnlyActivePropertiesForTenant() {
    UUID activeId = UUID.randomUUID();
    UUID inactiveId = UUID.randomUUID();

    when(propertyRepository.findAllByPropertyGroupId(TENANT_ID))
        .thenReturn(List.of(activeProperty(activeId), inactiveProperty(inactiveId)));

    List<PropertyListItemResponse> result = propertyManagementService.listProperties(adminPrincipal());

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(activeId);
    assertThat(result.get(0).isActive()).isTrue();
  }

  @Test
  void listProperties_throwsForNonAdmin() {
    assertThatThrownBy(() -> propertyManagementService.listProperties(viewerPrincipal()))
        .isInstanceOf(AuthException.class);
  }

  @Test
  void createProperty_createsAndReturnsDto() {
    Property created = Property.create(PROPERTY_ID, TENANT_ID, "New Property", true, "ref-123", Instant.now(), Instant.now());
    when(propertyRepository.create(eq(TENANT_ID), eq("New Property"), eq(true), eq("ref-123")))
        .thenReturn(created);

    PropertyListItemResponse result = propertyManagementService.createProperty(
        new CreatePropertyRequest("New Property", true, "ref-123"),
        adminPrincipal()
    );

    assertThat(result.id()).isEqualTo(PROPERTY_ID);
    assertThat(result.name()).isEqualTo("New Property");
    assertThat(result.isActive()).isTrue();
    assertThat(result.externalPropertyRef()).isEqualTo("ref-123");
  }

  @Test
  void createProperty_throwsForNonAdmin() {
    assertThatThrownBy(() -> propertyManagementService.createProperty(
        new CreatePropertyRequest("Name", true, null),
        viewerPrincipal()
    )).isInstanceOf(AuthException.class);
  }

  @Test
  void createProperty_throwsWhenNameIsBlank() {
    assertThatThrownBy(() -> propertyManagementService.createProperty(
        new CreatePropertyRequest("", true, null),
        adminPrincipal()
    )).isInstanceOf(domain.error.DomainError.ValidationError.class);
  }

  @Test
  void createProperty_throwsWhenRequestIsNull() {
    assertThatThrownBy(() -> propertyManagementService.createProperty(null, adminPrincipal()))
        .isInstanceOf(domain.error.DomainError.ValidationError.class);
  }

  @Test
  void deleteProperty_callsDeactivate() {
    propertyManagementService.deleteProperty(PROPERTY_ID, adminPrincipal());
    verify(propertyRepository).deactivate(PROPERTY_ID);
  }

  @Test
  void deleteProperty_throwsForNonAdmin() {
    assertThatThrownBy(() -> propertyManagementService.deleteProperty(PROPERTY_ID, viewerPrincipal()))
        .isInstanceOf(AuthException.class);
  }
}
