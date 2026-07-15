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
    String registerSource,
    String registerIp,
    String lastLoginAt,
    String lastLoginIp,
    String passwordUpdatedAt,
    String createdAt,
    String updatedAt,
    List<Long> boundWaybillIds,
    List<MemberWaybillSummaryResponse> waybills,
    List<MemberAuditLogResponse> auditLogs
) {
}
