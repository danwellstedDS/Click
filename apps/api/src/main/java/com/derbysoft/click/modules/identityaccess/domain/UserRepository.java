package com.derbysoft.click.modules.identityaccess.domain;

import com.derbysoft.click.modules.identityaccess.domain.aggregates.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
  Optional<User> findByEmail(String email);
  Optional<User> findById(UUID id);
  User create(String email, String passwordHash);
  void deleteById(UUID id);
}
