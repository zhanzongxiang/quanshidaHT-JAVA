package com.qsd.admin.waybill.dto;

public record WaybillSummaryResponse(
    Long id,
    String mainTrackingNo,
    String customerName,
    String destinationCountry,
    String destinationCity,
    String routeType,
    String currentStatus,
    String currentNode,
    String updatedAt
) {
}
