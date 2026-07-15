package com.qsd.admin.member.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record PackagePrealertSaveRequest(
    @NotBlank(message = "快递单号不能为空") String trackingNo,
    String courierCode,
    String warehouseCode,
    @NotBlank(message = "货物名称不能为空") String goodsName,
    @Min(value = 1, message = "包裹数量必须大于 0") Integer packageCount,
    BigDecimal estimatedWeight,
    String remark
) {
}
