package com.qsd.admin.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ReconcileCreateRequest(
    @NotNull(message = "reconcileDate must not be null")
    LocalDate reconcileDate,

    @NotBlank(message = "channel must not be blank")
    @Size(max = 32, message = "channel max length is 32")
    String channel,

    @NotBlank(message = "reconcileStatus must not be blank")
    @Size(max = 32, message = "reconcileStatus max length is 32")
    String reconcileStatus,

    Integer diffCount,

    String summary
) {
}
