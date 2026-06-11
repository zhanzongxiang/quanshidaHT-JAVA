package com.qsd.admin.member.dto;

import jakarta.validation.constraints.Size;

public record MemberProfileUpdateRequest(
    @Size(max = 64, message = "Nickname must be at most 64 characters")
    String nickname,

    @Size(max = 64, message = "Full name must be at most 64 characters")
    String fullName,

    @Size(max = 500, message = "Avatar URL must be at most 500 characters")
    String avatarUrl
) {
}
