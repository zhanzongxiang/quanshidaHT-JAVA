package com.qsd.admin.payment.dto;

import java.util.List;

public record ReconcileDiffDetailResponse(
    Long id,
    String reconcileDate,
    String channel,
    String reconcileStatus,
    Integer diffCount,
    List<String> diffItems,
    String summary
) {
}
