package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberWechatLoginRequest(
    @NotBlank(message = "WeChat login code is required")
    @Size(max = 128, message = "WeChat login code must be at most 128 characters")
    String code,

    @Pattern(regexp = "^$|^1\\d{10}$", message = "Phone must be an 11-digit mobile number")
    String phone,

    @Size(max = 64, message = "Nickname must be at most 64 characters")
    String nickname,

    @Size(max = 64, message = "Full name must be at most 64 characters")
    String fullName,

    Boolean replaceBinding
) {
}
