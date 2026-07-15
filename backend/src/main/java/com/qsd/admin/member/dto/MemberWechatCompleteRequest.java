package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberWechatCompleteRequest(
    @NotBlank(message = "WeChat bind ticket is required")
    String bindTicket,

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^1\\d{10}$", message = "Phone must be an 11-digit mobile number")
    String phone,

    @Size(max = 64, message = "Nickname must be at most 64 characters")
    String nickname,

    @Size(max = 64, message = "Full name must be at most 64 characters")
    String fullName,

    Boolean replaceBinding
) {
}
