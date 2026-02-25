package persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "properties")
public class PropertyEntity {
  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(name = "property_group_id", nullable = false)
  private UUID propertyGroupId;

  @Column(nullable = false)
  private String name;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  @Column(name = "external_property_ref")
  private String externalPropertyRef;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected PropertyEntity() {
  }

  public PropertyEntity(UUID propertyGroupId, String name) {
    this.propertyGroupId = propertyGroupId;
    this.name = name;
    this.isActive = true;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPropertyGroupId() {
    return propertyGroupId;
  }

  public String getName() {
    return name;
  }

  public boolean isActive() {
    return isActive;
  }

  public String getExternalPropertyRef() {
    return externalPropertyRef;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
