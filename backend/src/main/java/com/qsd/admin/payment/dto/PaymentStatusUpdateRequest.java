package com.qsd.admin.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaymentStatusUpdateRequest(
    @NotBlank(message = "支付状态不能为空")
    @Size(max = 32, message = "支付状态长度不能超过 32 个字符")
    String status,

    @Size(max = 64, message = "交易流水号长度不能超过 64 个字符")
    String externalTransactionNo
) {
}
