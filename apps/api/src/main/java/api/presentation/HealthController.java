package api.presentation;

import domain.Health;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  @GetMapping("/health")
  public Health health() {
    return new Health();
  }

  @GetMapping("/api/health")
  public Health apiHealth() {
    return new Health();
  }
}
