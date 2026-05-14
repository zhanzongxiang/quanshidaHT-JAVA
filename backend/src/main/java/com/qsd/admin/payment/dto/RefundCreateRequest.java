package com.qsd.admin.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RefundCreateRequest(
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于 0")
    BigDecimal amountRefund,

    @Size(max = 255, message = "退款原因长度不能超过 255 个字符")
    String reason
) {
}
