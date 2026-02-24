package persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "contract_coverages")
public class ContractCoverageEntity {
  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(name = "contract_id", nullable = false)
  private UUID contractId;

  @Column(name = "scope_id", nullable = false)
  private UUID scopeId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected ContractCoverageEntity() {
  }

  public ContractCoverageEntity(UUID contractId, UUID scopeId) {
    this.contractId = contractId;
    this.scopeId = scopeId;
  }

  public UUID getId() {
    return id;
  }

  public UUID getContractId() {
    return contractId;
  }

  public UUID getScopeId() {
    return scopeId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
