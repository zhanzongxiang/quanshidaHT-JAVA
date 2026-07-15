package com.qsd.admin.member.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminMemberShipmentResponse(
    Long id,
    Long memberId,
    String memberNo,
    String username,
    String shipmentNo,
    String shipmentStatus,
    Integer packageCount,
    BigDecimal totalWeight,
    String remark,
    String createdAt,
    List<Long> packageIds
) {
}
