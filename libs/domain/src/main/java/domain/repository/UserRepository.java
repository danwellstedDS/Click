package domain.repository;

import domain.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
  Optional<User> findByEmail(String email);
  Optional<User> findById(UUID id);
  User create(String email, String passwordHash);
  void deleteById(UUID id);
}
