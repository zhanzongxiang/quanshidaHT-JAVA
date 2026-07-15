package com.qsd.admin.payment.dto;

public record ReconcileRecordResponse(
    Long id,
    String reconcileDate,
    String channel,
    String reconcileStatus,
    Integer diffCount,
    String summary,
    String createdAt,
    String updatedAt
) {
}
