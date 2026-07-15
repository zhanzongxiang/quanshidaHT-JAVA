package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MemberShipmentCreateRequest(
    @NotNull(message = "收货地址不能为空") Long addressId,
    @NotEmpty(message = "至少选择一个包裹") List<Long> packageIds,
    String remark
) {
}
