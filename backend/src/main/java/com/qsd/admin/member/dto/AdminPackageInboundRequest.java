package com.qsd.admin.member.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record AdminPackageInboundRequest(
    String warehouseCode,
    @Min(value = 1, message = "包裹数量必须大于 0") Integer packageCount,
    @DecimalMin(value = "0.00", message = "重量不能小于 0") BigDecimal weightKg,
    String remark
) {
}
