package com.qsd.admin.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TenantDomainSaveRequest(
    @NotBlank(message = "domain must not be blank")
    @Size(max = 255, message = "domain max length is 255")
    String domain,

    @NotBlank(message = "domainType must not be blank")
    @Size(max = 32, message = "domainType max length is 32")
    String domainType,

    Boolean enabled
) {
}
