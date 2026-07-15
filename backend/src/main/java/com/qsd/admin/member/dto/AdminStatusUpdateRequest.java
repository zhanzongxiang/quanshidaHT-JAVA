package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminStatusUpdateRequest(
    @NotBlank(message = "状态不能为空") String status
) {
}
