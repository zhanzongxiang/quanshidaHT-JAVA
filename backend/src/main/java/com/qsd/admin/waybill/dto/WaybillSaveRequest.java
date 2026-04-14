package com.qsd.admin.waybill.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record WaybillSaveRequest(
    @NotBlank(message = "mainTrackingNo must not be blank")
    @Size(max = 64, message = "mainTrackingNo max length is 64")
    String mainTrackingNo,
    @Size(max = 64, message = "referenceNo max length is 64")
    String referenceNo,
    @NotBlank(message = "customerName must not be blank")
    @Size(max = 64, message = "customerName max length is 64")
    String customerName,
    @Size(max = 32, message = "customerPhone max length is 32")
    String customerPhone,
    @Size(max = 128, message = "originWarehouse max length is 128")
    String originWarehouse,
    @NotBlank(message = "destinationCountry must not be blank")
    @Size(max = 64, message = "destinationCountry max length is 64")
    String destinationCountry,
    @Size(max = 64, message = "destinationCity max length is 64")
    String destinationCity,
    @NotBlank(message = "routeType must not be blank")
    @Size(max = 32, message = "routeType max length is 32")
    String routeType,
    @NotBlank(message = "currentStatus must not be blank")
    @Size(max = 32, message = "currentStatus max length is 32")
    String currentStatus,
    @Size(max = 128, message = "currentNode max length is 128")
    String currentNode,
    @Size(max = 255, message = "cargoDescription max length is 255")
    String cargoDescription,
    @NotNull(message = "packageCount must not be null")
    Integer packageCount,
    BigDecimal weightKg,
    @Size(max = 500, message = "remark max length is 500")
    String remark,
    @Valid
    List<WaybillLegPayload> legs,
    @Valid
    List<WaybillEventPayload> events
) {
}
