package com.qsd.admin.member.dto;

import com.qsd.admin.waybill.dto.WaybillEventPayload;
import com.qsd.admin.waybill.dto.WaybillLegPayload;

import java.math.BigDecimal;
import java.util.List;

public record MemberWaybillDetailResponse(
    Long id,
    String mainTrackingNo,
    String referenceNo,
    String customerName,
    String destinationCountry,
    String destinationCity,
    String currentStatus,
    String currentNode,
    String originWarehouse,
    String cargoDescription,
    Integer packageCount,
    BigDecimal weightKg,
    String updatedAt,
    List<WaybillLegPayload> legs,
    List<WaybillEventPayload> events
) {
}
