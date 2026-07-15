package com.qsd.admin.payment.dto;

import jakarta.validation.constraints.NotNull;

public record PayMerchantActivateRequest(
    @NotNull(message = "merchantConfigId must not be null")
    Long merchantConfigId
) {
}
