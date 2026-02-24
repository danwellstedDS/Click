package domain.repository;

import domain.Hotel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HotelRepository {
  Optional<Hotel> findById(UUID id);
  List<Hotel> findAllByChainId(UUID chainId);
  Hotel create(UUID chainId, String name);
}
