package com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.repository;

import com.derbysoft.click.modules.organisationstructure.domain.PortfolioRepository;
import com.derbysoft.click.modules.organisationstructure.domain.aggregates.Portfolio;
import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.entity.PortfolioEntity;
import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.mapper.PortfolioMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

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
