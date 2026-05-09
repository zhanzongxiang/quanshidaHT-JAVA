package com.qsd.admin.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WechatPayCallbackRequest(
    @NotBlank(message = "orderNo must not be blank")
    @Size(max = 64, message = "orderNo max length is 64")
    String orderNo,

    @NotBlank(message = "transactionNo must not be blank")
    @Size(max = 64, message = "transactionNo max length is 64")
    String transactionNo,

    @NotBlank(message = "status must not be blank")
    @Size(max = 32, message = "status max length is 32")
    String status,

    String payload
) {
}
