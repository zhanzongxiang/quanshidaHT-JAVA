package com.qsd.admin.tenant.dto;

public record TenantDomainResponse(
    Long id,
    String domain,
    String domainType,
    boolean enabled,
    String createdAt
) {
}
