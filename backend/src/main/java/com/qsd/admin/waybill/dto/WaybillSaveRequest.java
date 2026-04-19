package com.qsd.admin.waybill.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record WaybillSaveRequest(
    @NotBlank(message = "主运单号不能为空")
    @Size(max = 64, message = "主运单号长度不能超过 64 个字符")
    String mainTrackingNo,
    @Size(max = 64, message = "参考单号长度不能超过 64 个字符")
    String referenceNo,
    @NotBlank(message = "客户名称不能为空")
    @Size(max = 64, message = "客户名称长度不能超过 64 个字符")
    String customerName,
    @Size(max = 32, message = "客户电话长度不能超过 32 个字符")
    String customerPhone,
    @Size(max = 128, message = "始发仓长度不能超过 128 个字符")
    String originWarehouse,
    @NotBlank(message = "目的国家不能为空")
    @Size(max = 64, message = "目的国家长度不能超过 64 个字符")
    String destinationCountry,
    @Size(max = 64, message = "目的城市长度不能超过 64 个字符")
    String destinationCity,
    @NotBlank(message = "线路类型不能为空")
    @Size(max = 32, message = "线路类型长度不能超过 32 个字符")
    String routeType,
    @NotBlank(message = "当前状态不能为空")
    @Size(max = 32, message = "当前状态长度不能超过 32 个字符")
    String currentStatus,
    @Size(max = 128, message = "当前节点长度不能超过 128 个字符")
    String currentNode,
    @Size(max = 255, message = "货物描述长度不能超过 255 个字符")
    String cargoDescription,
    @NotNull(message = "包裹数量不能为空")
    Integer packageCount,
    BigDecimal weightKg,
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    String remark,
    @Valid
    List<WaybillLegPayload> legs,
    @Valid
    List<WaybillEventPayload> events
) {
}
