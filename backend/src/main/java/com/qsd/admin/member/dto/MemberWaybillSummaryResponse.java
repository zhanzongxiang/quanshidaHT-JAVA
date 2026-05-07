package com.qsd.admin.member.dto;

public record MemberWaybillSummaryResponse(
    Long id,
    String mainTrackingNo,
    String referenceNo,
    String customerName,
    String destinationCountry,
    String destinationCity,
    String currentStatus,
    String currentNode,
    String updatedAt
) {
}
