package api.application.dto;

import domain.Role;

public record CreateUserRequest(String email, Role role) {}
