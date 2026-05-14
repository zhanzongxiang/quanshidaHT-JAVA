package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberWechatBindRequest(
    @NotBlank(message = "openid 不能为空")
    @Size(max = 64, message = "openid 长度不能超过 64 个字符")
    String openid,

    @Size(max = 64, message = "unionid 长度不能超过 64 个字符")
    String unionid
) {
}
