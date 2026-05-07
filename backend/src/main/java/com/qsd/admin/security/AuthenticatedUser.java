package com.qsd.admin.security;

import java.security.Principal;

public record AuthenticatedUser(
    Long userId,
    String username,
    String tokenType
) implements Principal {

    @Override
    public String getName() {
        return username;
    }
}
