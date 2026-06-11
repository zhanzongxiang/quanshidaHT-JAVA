package com.qsd.admin.auth.dto;

import jakarta.validation.constraints.NotNull;

public record SwitchTenantRequest(
    @NotNull(message = "tenantId must not be null")
    Long tenantId
) {
}
