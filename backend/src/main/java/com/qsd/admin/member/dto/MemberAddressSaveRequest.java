package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberAddressSaveRequest(
    @NotBlank(message = "联系人不能为空") String contactName,
    @NotBlank(message = "联系电话不能为空") String contactPhone,
    String country,
    String province,
    String city,
    String district,
    @NotBlank(message = "详细地址不能为空") String detailAddress,
    String postalCode,
    Boolean isDefault
) {
}
