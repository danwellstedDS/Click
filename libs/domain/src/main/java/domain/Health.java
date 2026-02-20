package domain;

public record Health(String status) {
  public Health() {
    this("ok");
  }
}
