package com.qsd.admin.member.dto;

public record MemberProfileResponse(
    Long id,
    String phone,
    String nickname,
    String fullName,
    String avatarUrl,
    String status,
    String createdAt
) {
}
