package com.derbysoft.click.modules.identityaccess.api.events;

import java.util.UUID;

public record UserCreated(UUID userId, String email) {}
