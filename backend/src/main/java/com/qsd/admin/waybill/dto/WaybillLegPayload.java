package com.qsd.admin.waybill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WaybillLegPayload(
    Integer legNo,
    @NotBlank(message = "分段类型不能为空")
    @Size(max = 32, message = "分段类型长度不能超过 32 个字符")
    String legType,
    @Size(max = 64, message = "承运商长度不能超过 64 个字符")
    String carrierName,
    @NotBlank(message = "分段运单号不能为空")
    @Size(max = 64, message = "分段运单号长度不能超过 64 个字符")
    String trackingNo,
    @Size(max = 128, message = "起始节点长度不能超过 128 个字符")
    String fromNode,
    @Size(max = 128, message = "目标节点长度不能超过 128 个字符")
    String toNode,
    @Size(max = 32, message = "分段状态长度不能超过 32 个字符")
    String legStatus,
    Boolean transferFlag,
    String departureTime,
    String arrivalTime,
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    String remark
) {
}
