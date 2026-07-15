package com.qsd.admin.member.dto;

public record MemberMeResponse(
    Long memberId,
    String memberNo,
    String username,
    String mobile,
    String nickname,
    String realName,
    String levelCode,
    String status
) {
}
