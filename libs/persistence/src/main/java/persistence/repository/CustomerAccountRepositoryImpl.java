package persistence.repository;

import domain.CustomerAccount;
import domain.CustomerType;
import domain.repository.CustomerAccountRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import persistence.entity.CustomerAccountEntity;
import persistence.mapper.CustomerAccountMapper;

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
