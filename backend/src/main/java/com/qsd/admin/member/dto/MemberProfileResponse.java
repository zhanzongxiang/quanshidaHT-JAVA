package com.qsd.admin.member.dto;

public record MemberProfileResponse(
    Long id,
    String phone,
    String wechatOpenid,
    String wechatUnionid,
    String wechatBindTime,
    String nickname,
    String fullName,
    String avatarUrl,
    String status,
    String createdAt
) {
}
