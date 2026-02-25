# PropertyGroup + Property Refactor — References

## Files Changed

### Deleted
- `libs/domain/src/main/java/domain/Chain.java`
- `libs/domain/src/main/java/domain/Hotel.java`
- `libs/domain/src/main/java/domain/PortfolioHotel.java`
- `libs/domain/src/main/java/domain/repository/ChainRepository.java`
- `libs/domain/src/main/java/domain/repository/HotelRepository.java`
- `libs/persistence/src/main/java/persistence/entity/ChainEntity.java`
- `libs/persistence/src/main/java/persistence/entity/HotelEntity.java`
- `libs/persistence/src/main/java/persistence/entity/PortfolioHotelEntity.java`
- `libs/persistence/src/main/java/persistence/mapper/ChainMapper.java`
- `libs/persistence/src/main/java/persistence/mapper/HotelMapper.java`
- `libs/persistence/src/main/java/persistence/repository/ChainJpaRepository.java`
- `libs/persistence/src/main/java/persistence/repository/HotelJpaRepository.java`
- `libs/persistence/src/main/java/persistence/repository/ChainRepositoryImpl.java`
- `libs/persistence/src/main/java/persistence/repository/HotelRepositoryImpl.java`

### Created
- `libs/domain/src/main/java/domain/PropertyGroup.java`
- `libs/domain/src/main/java/domain/Property.java`
- `libs/domain/src/main/java/domain/PortfolioProperty.java`
- `libs/domain/src/main/java/domain/repository/PropertyGroupRepository.java`
- `libs/domain/src/main/java/domain/repository/PropertyRepository.java`
- `libs/persistence/src/main/java/persistence/entity/PropertyGroupEntity.java`
- `libs/persistence/src/main/java/persistence/entity/PropertyEntity.java`
- `libs/persistence/src/main/java/persistence/entity/PortfolioPropertyEntity.java`
- `libs/persistence/src/main/java/persistence/mapper/PropertyGroupMapper.java`
- `libs/persistence/src/main/java/persistence/mapper/PropertyMapper.java`
- `libs/persistence/src/main/java/persistence/repository/PropertyGroupJpaRepository.java`
- `libs/persistence/src/main/java/persistence/repository/PropertyJpaRepository.java`
- `libs/persistence/src/main/java/persistence/repository/PropertyGroupRepositoryImpl.java`
- `libs/persistence/src/main/java/persistence/repository/PropertyRepositoryImpl.java`
- `infra/db/migrations/V202602250001__replace_chain_hotel_with_property_group.sql`

### Modified
- `libs/domain/src/main/java/domain/ScopeType.java`
- `libs/domain/src/main/java/domain/AccessScope.java`
- `libs/domain/src/main/java/domain/Portfolio.java`
- `libs/domain/src/main/java/domain/repository/AccessScopeRepository.java`
- `libs/persistence/src/main/java/persistence/entity/AccessScopeEntity.java`
- `libs/persistence/src/main/java/persistence/entity/PortfolioEntity.java`
- `libs/persistence/src/main/java/persistence/mapper/AccessScopeMapper.java`
- `libs/persistence/src/main/java/persistence/mapper/PortfolioMapper.java`
- `libs/persistence/src/main/java/persistence/repository/AccessScopeJpaRepository.java`
- `libs/persistence/src/main/java/persistence/repository/AccessScopeRepositoryImpl.java`
- `apps/api/src/main/java/api/application/AuthService.java`
- `apps/api/src/main/java/api/application/UserManagementService.java`
- `apps/web/src/components/AppLayout.tsx`
- `apps/api/src/test/java/api/application/AuthServiceTest.java`

## Prior Migration Replaced
- `V202602230009__update_seed.sql` — seed data for chains/hotels; superseded by new migration.
