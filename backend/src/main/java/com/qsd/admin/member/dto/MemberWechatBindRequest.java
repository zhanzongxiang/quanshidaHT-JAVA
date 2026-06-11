package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberWechatBindRequest(
    @NotBlank(message = "WeChat auth code is required")
    @Size(max = 128, message = "WeChat auth code must be at most 128 characters")
    String code,
    Boolean replaceBinding
) {
}
