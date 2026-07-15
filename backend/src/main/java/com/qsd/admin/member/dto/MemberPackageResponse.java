package com.qsd.admin.member.dto;

import java.math.BigDecimal;

public record MemberPackageResponse(
    Long id,
    String packageNo,
    String trackingNo,
    String goodsName,
    String warehouseCode,
    Integer packageCount,
    BigDecimal weightKg,
    String packageStatus,
    boolean issueFlag,
    String issueType,
    String issueNote,
    String warehouseInAt
) {
}
