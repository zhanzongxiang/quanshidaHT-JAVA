package com.qsd.admin.member.dto;

import java.math.BigDecimal;

public record MemberOrderResponse(
    Long id,
    String orderNo,
    String orderStatus,
    String paymentStatus,
    BigDecimal amount,
    String currencyCode,
    String remark,
    String createdAt
) {
}
