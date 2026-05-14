package com.qsd.admin.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PaymentAdminCreateRequest(
    @NotNull(message = "会员不能为空")
    Long memberId,

    Long merchantConfigId,

    Long waybillId,

    @NotBlank(message = "业务类型不能为空")
    @Size(max = 32, message = "业务类型长度不能超过 32 个字符")
    String businessType,

    @NotBlank(message = "场景类型不能为空")
    @Size(max = 32, message = "场景类型长度不能超过 32 个字符")
    String sceneType,

    @NotBlank(message = "支付渠道不能为空")
    @Size(max = 32, message = "支付渠道长度不能超过 32 个字符")
    String channel,

    @NotBlank(message = "币种不能为空")
    @Size(max = 16, message = "币种长度不能超过 16 个字符")
    String currency,

    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于 0")
    BigDecimal amountTotal,

    @Size(max = 255, message = "支付描述长度不能超过 255 个字符")
    String description,

    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    String remark
) {
}
