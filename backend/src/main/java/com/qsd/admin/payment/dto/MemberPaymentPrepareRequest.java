package com.qsd.admin.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MemberPaymentPrepareRequest(
    @NotNull(message = "运单 ID 不能为空")
    Long waybillId,

    @NotNull(message = "支付金额不能为空")
    BigDecimal amountTotal,

    @Size(max = 255, message = "支付描述长度不能超过 255 个字符")
    String description,

    @NotBlank(message = "支付渠道不能为空")
    @Size(max = 32, message = "支付渠道长度不能超过 32 个字符")
    String channel
) {
}
