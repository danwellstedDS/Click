package com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.entity;

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
@Table(name = "property_groups")
public class PropertyGroupEntity {
  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(name = "parent_id")
  private UUID parentId;

  @Column(nullable = false)
  private String name;

  @Column
  private String timezone;

  @Column
  private String currency;

  @Column(name = "primary_org_id")
  private UUID primaryOrgId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected PropertyGroupEntity() {}

  public PropertyGroupEntity(String name) {
    this.name = name;
  }

  public UUID getId() { return id; }
  public UUID getParentId() { return parentId; }
  public String getName() { return name; }
  public String getTimezone() { return timezone; }
  public String getCurrency() { return currency; }
  public UUID getPrimaryOrgId() { return primaryOrgId; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
