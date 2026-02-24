package persistence.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import persistence.entity.HotelEntity;

public interface HotelJpaRepository extends JpaRepository<HotelEntity, UUID> {
  List<HotelEntity> findAllByChainId(UUID chainId);
}
