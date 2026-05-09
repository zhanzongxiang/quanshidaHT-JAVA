package com.qsd.admin.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefundCallbackRequest(
    @NotBlank(message = "refundNo must not be blank")
    @Size(max = 64, message = "refundNo max length is 64")
    String refundNo,

    @NotBlank(message = "status must not be blank")
    @Size(max = 32, message = "status max length is 32")
    String status,

    @Size(max = 64, message = "externalRefundNo max length is 64")
    String externalRefundNo,

    String payload
) {
}
