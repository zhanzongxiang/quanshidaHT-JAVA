package com.qsd.admin.payment.dto;

public record NotifyFailureStatResponse(
    String category,
    Integer count,
    String latestCreatedAt
) {
}
