package com.qsd.admin.member.dto;

public record MemberAdminSummaryResponse(
    Long id,
    String phone,
    String wechatOpenid,
    String wechatUnionid,
    String wechatBindTime,
    String nickname,
    String fullName,
    String status,
    long waybillCount,
    String lastLoginAt,
    String createdAt
) {
}
