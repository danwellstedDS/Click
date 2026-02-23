package api.application.dto;

public record CreateUserResponse(UserListItemResponse user, String temporaryPassword) {}
