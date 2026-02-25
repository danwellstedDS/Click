package persistence.repository;

import domain.Portfolio;
import domain.repository.PortfolioRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import persistence.entity.PortfolioEntity;
import persistence.mapper.PortfolioMapper;

@Repository
public class PortfolioRepositoryImpl implements PortfolioRepository {
  private final PortfolioJpaRepository portfolioJpaRepository;

  public PortfolioRepositoryImpl(PortfolioJpaRepository portfolioJpaRepository) {
    this.portfolioJpaRepository = portfolioJpaRepository;
  }

  @Override
  public Optional<Portfolio> findById(UUID id) {
    return portfolioJpaRepository.findById(id).map(PortfolioMapper::toDomain);
  }

  @Override
  public List<Portfolio> findAllByPropertyGroupId(UUID propertyGroupId) {
    return portfolioJpaRepository.findAllByPropertyGroupId(propertyGroupId).stream()
        .map(PortfolioMapper::toDomain)
        .toList();
  }

  @Override
  public Portfolio create(UUID propertyGroupId, String name) {
    PortfolioEntity entity = new PortfolioEntity(propertyGroupId, name);
    return PortfolioMapper.toDomain(portfolioJpaRepository.saveAndFlush(entity));
  }
}
