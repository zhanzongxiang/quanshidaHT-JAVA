package com.qsd.admin.payment.dto;

public record NotifyReplayResponse(
    Long sourceLogId,
    String replayType,
    String replayStatus,
    String message
) {
}
