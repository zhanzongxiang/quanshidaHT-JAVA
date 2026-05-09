package com.qsd.admin.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MemberPaymentPrepareRequest(
    @NotNull(message = "waybillId must not be null")
    Long waybillId,

    @NotNull(message = "amountTotal must not be null")
    BigDecimal amountTotal,

    @Size(max = 255, message = "description max length is 255")
    String description,

    @NotBlank(message = "channel must not be blank")
    String channel
) {
}
