package com.qsd.admin.member.dto;

import java.math.BigDecimal;

public record PackagePrealertResponse(
    Long id,
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
