package com.qsd.admin.payment.dto;

import java.math.BigDecimal;

public record RefundOrderResponse(
    Long id,
    String refundNo,
    BigDecimal amountRefund,
    String status,
    String reason,
    String externalRefundNo,
    String refundedAt,
    String createdAt
) {
}
