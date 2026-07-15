package com.qsd.admin.member.dto;

import java.math.BigDecimal;

public record AdminMemberPrealertResponse(
    Long id,
    Long memberId,
    String memberNo,
    String username,
    String prealertNo,
    String trackingNo,
    String courierCode,
    String warehouseCode,
    String goodsName,
    Integer packageCount,
    BigDecimal estimatedWeight,
    String remark,
    String status,
    String createdAt
) {
}
