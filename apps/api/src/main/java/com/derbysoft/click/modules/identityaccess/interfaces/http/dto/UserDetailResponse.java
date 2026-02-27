package com.derbysoft.click.modules.identityaccess.interfaces.http.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserDetailResponse(
    UUID id,
    String email,
    String role,
    Instant createdAt,
    Instant updatedAt,
    List<MembershipInfo> memberships
) {
  public record MembershipInfo(String tenantId, String role, Instant memberSince) {}
}
