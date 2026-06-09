package com.qsd.admin.tenant.dto;

import java.util.List;

public record TenantResponse(
    Long id,
    String tenantCode,
    String tenantName,
    String status,
    String timezone,
    String locale,
    String remark,
    List<TenantDomainResponse> domains,
    TenantBootstrapAdminResponse bootstrapAdmin,
    String createdAt,
    String updatedAt
) {
}
