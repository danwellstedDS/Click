package com.derbysoft.click.modules.identityaccess.interfaces.http.dto;

import java.time.Instant;
import java.util.UUID;

public record UserListItemResponse(
    UUID id,
    String email,
    String role,
    Instant createdAt
) {}
