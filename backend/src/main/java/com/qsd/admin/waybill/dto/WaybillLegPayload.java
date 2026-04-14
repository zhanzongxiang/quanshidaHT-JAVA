package com.qsd.admin.waybill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WaybillLegPayload(
    Integer legNo,
    @NotBlank(message = "legType must not be blank")
    @Size(max = 32, message = "legType max length is 32")
    String legType,
    @Size(max = 64, message = "carrierName max length is 64")
    String carrierName,
    @NotBlank(message = "trackingNo must not be blank")
    @Size(max = 64, message = "trackingNo max length is 64")
    String trackingNo,
    @Size(max = 128, message = "fromNode max length is 128")
    String fromNode,
    @Size(max = 128, message = "toNode max length is 128")
    String toNode,
    @Size(max = 32, message = "legStatus max length is 32")
    String legStatus,
    Boolean transferFlag,
    String departureTime,
    String arrivalTime,
    @Size(max = 500, message = "remark max length is 500")
    String remark
) {
}
