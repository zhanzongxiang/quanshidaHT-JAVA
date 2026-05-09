package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberWechatLoginRequest(
    @NotBlank(message = "code must not be blank")
    @Size(max = 128, message = "code max length is 128")
    String code,

    @Size(max = 64, message = "phone max length is 64")
    String phone,

    @Size(max = 64, message = "nickname max length is 64")
    String nickname,

    @Size(max = 64, message = "fullName max length is 64")
    String fullName
) {
}
