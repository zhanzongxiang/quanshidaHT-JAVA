package com.qsd.admin.member.dto;

import jakarta.validation.constraints.Size;

public record MemberProfileUpdateRequest(
    @Size(max = 64, message = "nickname max length is 64")
    String nickname,

    @Size(max = 64, message = "fullName max length is 64")
    String fullName,

    @Size(max = 500, message = "avatarUrl max length is 500")
    String avatarUrl
) {
}
