package com.qsd.admin.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaymentStatusUpdateRequest(
    @NotBlank(message = "status must not be blank")
    String status,

    @Size(max = 64, message = "externalTransactionNo max length is 64")
    String externalTransactionNo
) {
}
