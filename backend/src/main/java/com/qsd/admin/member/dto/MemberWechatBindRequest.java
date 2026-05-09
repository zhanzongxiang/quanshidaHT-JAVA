package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberWechatBindRequest(
    @NotBlank(message = "openid must not be blank")
    @Size(max = 64, message = "openid max length is 64")
    String openid,

    @Size(max = 64, message = "unionid max length is 64")
    String unionid
) {
}
