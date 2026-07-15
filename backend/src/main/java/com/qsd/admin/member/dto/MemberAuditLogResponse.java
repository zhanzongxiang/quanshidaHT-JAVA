package com.qsd.admin.member.dto;

public record MemberAuditLogResponse(
    Long id,
    String actionType,
    String operatorType,
    String operatorLabel,
    String summary,
    String createdAt
) {
}
