package domain.repository;

import domain.AccessScope;
import domain.ScopeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccessScopeRepository {
  List<AccessScope> findByChainId(UUID chainId);
  Optional<AccessScope> findByChainIdAndType(UUID chainId, ScopeType type);
  List<AccessScope> findByHotelId(UUID hotelId);
  AccessScope create(ScopeType type, UUID chainId, UUID hotelId, UUID portfolioId);
}
