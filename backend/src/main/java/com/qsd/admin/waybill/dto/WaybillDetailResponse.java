package com.qsd.admin.waybill.dto;

import java.math.BigDecimal;
import java.util.List;

public record WaybillDetailResponse(
    Long id,
    String mainTrackingNo,
    String referenceNo,
    String customerName,
    String customerPhone,
    String originWarehouse,
    String destinationCountry,
    String destinationCity,
    String routeType,
    String currentStatus,
    String currentNode,
    String cargoDescription,
    Integer packageCount,
    BigDecimal weightKg,
    String remark,
    String createdAt,
    String updatedAt,
    List<WaybillLegPayload> legs,
    List<WaybillEventPayload> events
) {
}
