package com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.repository;

import com.derbysoft.click.modules.tenantgovernance.domain.CustomerAccountRepository;
import com.derbysoft.click.modules.tenantgovernance.domain.aggregates.CustomerAccount;
import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.CustomerType;
import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.entity.CustomerAccountEntity;
import com.derbysoft.click.modules.tenantgovernance.infrastructure.persistence.mapper.CustomerAccountMapper;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerAccountRepositoryImpl implements CustomerAccountRepository {
  private final CustomerAccountJpaRepository customerAccountJpaRepository;

  public CustomerAccountRepositoryImpl(CustomerAccountJpaRepository customerAccountJpaRepository) {
    this.customerAccountJpaRepository = customerAccountJpaRepository;
  }

  @Override
  public Optional<CustomerAccount> findById(UUID id) {
    return customerAccountJpaRepository.findById(id).map(CustomerAccountMapper::toDomain);
  }

  @Override
  public CustomerAccount create(CustomerType type, String name) {
    CustomerAccountEntity entity = new CustomerAccountEntity(type, name);
    return CustomerAccountMapper.toDomain(customerAccountJpaRepository.saveAndFlush(entity));
  }
}
