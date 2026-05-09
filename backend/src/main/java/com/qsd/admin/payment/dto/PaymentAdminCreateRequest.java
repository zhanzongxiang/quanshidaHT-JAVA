package com.qsd.admin.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PaymentAdminCreateRequest(
    @NotNull(message = "memberId must not be null")
    Long memberId,

    Long merchantConfigId,

    Long waybillId,

    @NotBlank(message = "businessType must not be blank")
    @Size(max = 32, message = "businessType max length is 32")
    String businessType,

    @NotBlank(message = "sceneType must not be blank")
    @Size(max = 32, message = "sceneType max length is 32")
    String sceneType,

    @NotBlank(message = "channel must not be blank")
    @Size(max = 32, message = "channel max length is 32")
    String channel,

    @NotBlank(message = "currency must not be blank")
    @Size(max = 16, message = "currency max length is 16")
    String currency,

    @NotNull(message = "amountTotal must not be null")
    @DecimalMin(value = "0.01", message = "amountTotal must be greater than 0")
    BigDecimal amountTotal,

    @Size(max = 255, message = "description max length is 255")
    String description,

    @Size(max = 500, message = "remark max length is 500")
    String remark
) {
}
