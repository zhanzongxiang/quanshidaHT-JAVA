package com.qsd.admin.payment.dto;

import java.math.BigDecimal;

public record MemberPayOrderSummaryResponse(
    Long id,
    String orderNo,
    Long merchantConfigId,
    String merchantName,
    Long waybillId,
    String waybillTrackingNo,
    String channel,
    BigDecimal amountTotal,
    BigDecimal amountPaid,
    String status,
    String description,
    String paidAt,
    String createdAt
) {
}
