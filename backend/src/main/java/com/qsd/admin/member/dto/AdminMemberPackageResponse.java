package com.qsd.admin.member.dto;

import java.math.BigDecimal;

public record AdminMemberPackageResponse(
    Long id,
    Long memberId,
    String memberNo,
    String username,
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
