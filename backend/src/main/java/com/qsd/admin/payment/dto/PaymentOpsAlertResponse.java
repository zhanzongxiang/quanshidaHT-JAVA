package com.qsd.admin.payment.dto;

public record PaymentOpsAlertResponse(
    String severity,
    String rule,
    String title,
    String message,
    String suggestedAction
) {
}
