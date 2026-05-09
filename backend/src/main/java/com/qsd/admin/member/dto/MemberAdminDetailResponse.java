package com.qsd.admin.member.dto;

import java.util.List;

public record MemberAdminDetailResponse(
    Long id,
    String phone,
    String wechatOpenid,
    String wechatUnionid,
    String wechatBindTime,
    String nickname,
    String fullName,
    String avatarUrl,
    String status,
    String remark,
    String lastLoginAt,
    String createdAt,
    String updatedAt,
    List<Long> boundWaybillIds,
    List<MemberWaybillSummaryResponse> waybills
) {
}
