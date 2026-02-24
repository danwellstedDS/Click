package persistence.repository;

import domain.Chain;
import domain.repository.ChainRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import persistence.entity.ChainEntity;
import persistence.mapper.ChainMapper;

@Repository
public class ChainRepositoryImpl implements ChainRepository {
  private final ChainJpaRepository chainJpaRepository;

  public ChainRepositoryImpl(ChainJpaRepository chainJpaRepository) {
    this.chainJpaRepository = chainJpaRepository;
  }

  @Override
  public Optional<Chain> findById(UUID id) {
    return chainJpaRepository.findById(id).map(ChainMapper::toDomain);
  }

  @Override
  public Optional<Chain> findByPrimaryOrgId(UUID primaryOrgId) {
    return chainJpaRepository.findByPrimaryOrgId(primaryOrgId).map(ChainMapper::toDomain);
  }

  @Override
  public Chain create(String name) {
    ChainEntity entity = new ChainEntity(name);
    return ChainMapper.toDomain(chainJpaRepository.saveAndFlush(entity));
  }
}
