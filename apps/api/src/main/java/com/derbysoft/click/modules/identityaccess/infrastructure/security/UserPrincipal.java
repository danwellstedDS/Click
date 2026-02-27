package com.derbysoft.click.modules.identityaccess.infrastructure.security;

import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {
  private final UUID userId;
  private final UUID tenantId;
  private final String email;
  private final Role role;

  public UserPrincipal(UUID userId, UUID tenantId, String email, Role role) {
    this.userId = userId;
    this.tenantId = tenantId;
    this.email = email;
    this.role = role;
  }

  public UUID userId() { return userId; }
  public UUID tenantId() { return tenantId; }
  public Role role() { return role; }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getPassword() { return ""; }

  @Override
  public String getUsername() { return email; }

  @Override
  public boolean isAccountNonExpired() { return true; }

  @Override
  public boolean isAccountNonLocked() { return true; }

  @Override
  public boolean isCredentialsNonExpired() { return true; }

  @Override
  public boolean isEnabled() { return true; }
}
