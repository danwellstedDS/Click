package persistence.repository;

import domain.OrgMembership;
import domain.repository.OrgMembershipRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import persistence.entity.OrgMembershipEntity;
import persistence.mapper.OrgMembershipMapper;

@Repository
public class OrgMembershipRepositoryImpl implements OrgMembershipRepository {
  private final OrgMembershipJpaRepository orgMembershipJpaRepository;

  public OrgMembershipRepositoryImpl(OrgMembershipJpaRepository orgMembershipJpaRepository) {
    this.orgMembershipJpaRepository = orgMembershipJpaRepository;
  }

  @Override
  public List<OrgMembership> findByUserId(UUID userId) {
    return orgMembershipJpaRepository.findByUserId(userId).stream()
        .map(OrgMembershipMapper::toDomain)
        .toList();
  }

  @Override
  public List<OrgMembership> findByOrganizationId(UUID organizationId) {
    return orgMembershipJpaRepository.findByOrganizationId(organizationId).stream()
        .map(OrgMembershipMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<OrgMembership> findByUserAndOrganization(UUID userId, UUID organizationId) {
    return orgMembershipJpaRepository.findByUserIdAndOrganizationId(userId, organizationId)
        .map(OrgMembershipMapper::toDomain);
  }

  @Override
  public OrgMembership create(UUID userId, UUID organizationId, boolean isOrgAdmin) {
    OrgMembershipEntity entity = new OrgMembershipEntity(userId, organizationId, isOrgAdmin);
    return OrgMembershipMapper.toDomain(orgMembershipJpaRepository.saveAndFlush(entity));
  }
}
