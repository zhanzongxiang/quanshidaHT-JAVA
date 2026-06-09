package com.qsd.admin.security;

import java.security.Principal;

public record AuthenticatedUser(
    Long userId,
    String username,
    String tokenType,
    Long tenantId,
    String tenantCode
) implements Principal {

    @Override
    public String getName() {
        return username;
    }
}
