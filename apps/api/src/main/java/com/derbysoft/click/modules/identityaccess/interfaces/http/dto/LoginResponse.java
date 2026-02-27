package com.derbysoft.click.modules.identityaccess.interfaces.http.dto;

import java.util.List;

public record LoginResponse(
    String token,
    String refreshToken,
    UserInfo user,
    List<TenantInfo> tenants
) {}
