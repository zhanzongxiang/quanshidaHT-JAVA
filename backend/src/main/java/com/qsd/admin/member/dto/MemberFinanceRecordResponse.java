package com.qsd.admin.member.dto;

import java.math.BigDecimal;

public record MemberFinanceRecordResponse(
    Long id,
    String recordNo,
    String recordType,
    BigDecimal amount,
    String currencyCode,
    String recordStatus,
    String note,
    String createdAt
) {
}
