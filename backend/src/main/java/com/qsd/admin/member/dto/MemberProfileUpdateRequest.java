package com.qsd.admin.member.dto;

import jakarta.validation.constraints.Size;

public record MemberProfileUpdateRequest(
    @Size(max = 64, message = "昵称长度不能超过 64 个字符")
    String nickname,

    @Size(max = 64, message = "姓名长度不能超过 64 个字符")
    String fullName,

    @Size(max = 500, message = "头像地址长度不能超过 500 个字符")
    String avatarUrl
) {
}
