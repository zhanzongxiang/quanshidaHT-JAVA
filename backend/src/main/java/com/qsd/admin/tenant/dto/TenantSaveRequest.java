package com.qsd.admin.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TenantSaveRequest(
    @NotBlank(message = "tenantCode must not be blank")
    @Size(max = 64, message = "tenantCode max length is 64")
    String tenantCode,

    @NotBlank(message = "tenantName must not be blank")
    @Size(max = 128, message = "tenantName max length is 128")
    String tenantName,

    @NotBlank(message = "status must not be blank")
    @Size(max = 32, message = "status max length is 32")
    String status,

    @NotBlank(message = "timezone must not be blank")
    @Size(max = 64, message = "timezone max length is 64")
    String timezone,

    @NotBlank(message = "locale must not be blank")
    @Size(max = 32, message = "locale max length is 32")
    String locale,

    @Size(max = 500, message = "remark max length is 500")
    String remark,

    List<TenantDomainSaveRequest> domains
) {
}
