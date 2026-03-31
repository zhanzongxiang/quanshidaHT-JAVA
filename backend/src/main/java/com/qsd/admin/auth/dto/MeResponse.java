package com.qsd.admin.auth.dto;

import java.util.List;

public record MeResponse(
    Long userId,
    String username,
    List<String> permissions,
    List<MenuNode> menus
) {
    public record MenuNode(
        Long id,
        String name,
        String path,
        String component,
        String icon,
        List<MenuNode> children
    ) {
    }
}
