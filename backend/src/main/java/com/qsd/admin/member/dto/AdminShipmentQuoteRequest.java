package com.qsd.admin.member.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AdminShipmentQuoteRequest(
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.00", message = "金额不能小于 0")
    BigDecimal amount,
    String remark
) {
}
