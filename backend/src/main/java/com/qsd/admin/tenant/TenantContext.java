package com.qsd.admin.tenant;

public record TenantContext(
    Long tenantId,
    String tenantCode,
    String tenantName
) {
}
