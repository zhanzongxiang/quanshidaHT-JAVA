package com.qsd.admin.payment.dto;

public record PaymentNotifyLogResponse(
    Long id,
    String notifyType,
    String resourceId,
    String notifyStatus,
    String processResult,
    String notifiedAt,
    String createdAt
) {
}
