package persistence.repository;

import domain.Hotel;
import domain.repository.HotelRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import persistence.entity.HotelEntity;
import persistence.mapper.HotelMapper;

@Repository
public class HotelRepositoryImpl implements HotelRepository {
  private final HotelJpaRepository hotelJpaRepository;

  public HotelRepositoryImpl(HotelJpaRepository hotelJpaRepository) {
    this.hotelJpaRepository = hotelJpaRepository;
  }

  @Override
  public Optional<Hotel> findById(UUID id) {
    return hotelJpaRepository.findById(id).map(HotelMapper::toDomain);
  }

  @Override
  public List<Hotel> findAllByChainId(UUID chainId) {
    return hotelJpaRepository.findAllByChainId(chainId).stream()
        .map(HotelMapper::toDomain)
        .toList();
  }

  @Override
  public Hotel create(UUID chainId, String name) {
    HotelEntity entity = new HotelEntity(chainId, name);
    return HotelMapper.toDomain(hotelJpaRepository.saveAndFlush(entity));
  }
}
