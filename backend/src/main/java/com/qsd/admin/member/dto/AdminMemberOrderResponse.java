package com.qsd.admin.member.dto;

import java.math.BigDecimal;

public record AdminMemberOrderResponse(
    Long id,
    Long memberId,
    String memberNo,
    String username,
    Long shipmentId,
    String orderNo,
    String orderStatus,
    String paymentStatus,
    BigDecimal amount,
    String currencyCode,
    String remark,
    String createdAt
) {
}
