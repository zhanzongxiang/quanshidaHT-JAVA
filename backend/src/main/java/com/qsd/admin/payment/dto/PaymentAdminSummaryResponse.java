package com.qsd.admin.payment.dto;

import java.math.BigDecimal;

public record PaymentAdminSummaryResponse(
    Long id,
    String orderNo,
    Long memberId,
    Long merchantConfigId,
    String merchantName,
    String merchantMchId,
    String merchantAppId,
    String memberPhone,
    Long waybillId,
    String waybillTrackingNo,
    String businessType,
    String sceneType,
    String channel,
    String currency,
    BigDecimal amountTotal,
    BigDecimal amountPaid,
    String status,
    String externalTransactionNo,
    String paidAt,
    String createdAt
) {
}
