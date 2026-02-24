package persistence.repository;

import domain.Organization;
import domain.OrganizationType;
import domain.repository.OrganizationRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import persistence.entity.OrganizationEntity;
import persistence.mapper.OrganizationMapper;

@Repository
public class OrganizationRepositoryImpl implements OrganizationRepository {
  private final OrganizationJpaRepository organizationJpaRepository;

  public OrganizationRepositoryImpl(OrganizationJpaRepository organizationJpaRepository) {
    this.organizationJpaRepository = organizationJpaRepository;
  }

  @Override
  public Optional<Organization> findById(UUID id) {
    return organizationJpaRepository.findById(id).map(OrganizationMapper::toDomain);
  }

  @Override
  public Organization create(String name, OrganizationType type) {
    OrganizationEntity entity = new OrganizationEntity(name, type);
    return OrganizationMapper.toDomain(organizationJpaRepository.saveAndFlush(entity));
  }
}
