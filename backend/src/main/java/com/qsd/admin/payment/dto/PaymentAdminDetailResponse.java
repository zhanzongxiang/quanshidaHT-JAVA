package com.qsd.admin.payment.dto;

import java.math.BigDecimal;
import java.util.List;

public record PaymentAdminDetailResponse(
    Long id,
    String orderNo,
    Long memberId,
    Long merchantConfigId,
    String merchantName,
    String merchantMchId,
    String merchantAppId,
    String memberPhone,
    String memberNickname,
    Long waybillId,
    String waybillTrackingNo,
    String businessType,
    String sceneType,
    String channel,
    String currency,
    BigDecimal amountTotal,
    BigDecimal amountPaid,
    String status,
    String description,
    String externalTransactionNo,
    String paidAt,
    String expiredAt,
    String closedAt,
    String refundedAt,
    String remark,
    String createdAt,
    String updatedAt,
    List<PaymentTransactionResponse> transactions,
    List<RefundOrderResponse> refunds,
    List<PaymentNotifyLogResponse> notifyLogs,
    List<RefundNotifyLogResponse> refundNotifyLogs
) {
}
