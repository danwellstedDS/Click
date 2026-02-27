package com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.entity;

import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.ContractStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "contracts")
public class ContractEntity {
  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(name = "customer_account_id", nullable = false)
  private UUID customerAccountId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ContractStatus status;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected ContractEntity() {}

  public ContractEntity(UUID customerAccountId, ContractStatus status, LocalDate startDate) {
    this.customerAccountId = customerAccountId;
    this.status = status;
    this.startDate = startDate;
  }

  public UUID getId() { return id; }
  public UUID getCustomerAccountId() { return customerAccountId; }
  public ContractStatus getStatus() { return status; }
  public LocalDate getStartDate() { return startDate; }
  public LocalDate getEndDate() { return endDate; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
