package com.qsd.admin.member.dto;

import java.math.BigDecimal;
import java.util.List;

public record MemberShipmentResponse(
    Long id,
    String shipmentNo,
    String shipmentStatus,
    Integer packageCount,
    BigDecimal totalWeight,
    String remark,
    String createdAt,
    List<Long> packageIds
) {
}
