package com.derbysoft.click.modules.identityaccess.interfaces.http.dto;

import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;

public record CreateUserRequest(String email, Role role) {}
