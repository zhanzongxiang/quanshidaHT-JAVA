package com.qsd.admin.tenant.dto;

public record TenantBootstrapAdminResponse(
    String username,
    String initialPassword
) {
}
