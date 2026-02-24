package domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class Contract {
  private final UUID id;
  private final UUID customerAccountId;
  private final ContractStatus status;
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final Instant createdAt;
  private final Instant updatedAt;

  private Contract(UUID id, UUID customerAccountId, ContractStatus status, LocalDate startDate, LocalDate endDate, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.customerAccountId = customerAccountId;
    this.status = status;
    this.startDate = startDate;
    this.endDate = endDate;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Contract create(UUID id, UUID customerAccountId, ContractStatus status, LocalDate startDate, LocalDate endDate, Instant createdAt, Instant updatedAt) {
    return new Contract(id, customerAccountId, status, startDate, endDate, createdAt, updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public UUID getCustomerAccountId() {
    return customerAccountId;
  }

  public ContractStatus getStatus() {
    return status;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
