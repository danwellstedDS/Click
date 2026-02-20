package domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RoleTest {
  @Test
  void shouldContainAdminAndViewer() {
    assertThat(Role.values()).contains(Role.ADMIN, Role.VIEWER);
  }
}
