package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberStatusUpdateRequest(
    @NotBlank(message = "会员状态不能为空")
    String status
) {
}
