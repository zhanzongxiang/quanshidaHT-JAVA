package com.qsd.admin.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RefundCreateRequest(
    @NotNull(message = "amountRefund must not be null")
    @DecimalMin(value = "0.01", message = "amountRefund must be greater than 0")
    BigDecimal amountRefund,

    @Size(max = 255, message = "reason max length is 255")
    String reason
) {
}
