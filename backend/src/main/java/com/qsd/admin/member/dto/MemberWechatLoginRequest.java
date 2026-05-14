package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberWechatLoginRequest(
    @NotBlank(message = "微信登录 code 不能为空")
    @Size(max = 128, message = "微信登录 code 长度不能超过 128 个字符")
    String code,

    @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确")
    String phone,

    @Size(max = 64, message = "昵称长度不能超过 64 个字符")
    String nickname,

    @Size(max = 64, message = "姓名长度不能超过 64 个字符")
    String fullName
) {
}
