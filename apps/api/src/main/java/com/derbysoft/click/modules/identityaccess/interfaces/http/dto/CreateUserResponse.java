package com.derbysoft.click.modules.identityaccess.interfaces.http.dto;

public record CreateUserResponse(UserListItemResponse user, String temporaryPassword) {}
