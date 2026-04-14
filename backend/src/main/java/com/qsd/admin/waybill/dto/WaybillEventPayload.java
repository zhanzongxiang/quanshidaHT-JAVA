package com.qsd.admin.waybill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WaybillEventPayload(
    Long legId,
    Integer sortNo,
    @NotBlank(message = "eventTime must not be blank")
    String eventTime,
    @NotBlank(message = "eventStatus must not be blank")
    @Size(max = 64, message = "eventStatus max length is 64")
    String eventStatus,
    @NotBlank(message = "eventDescription must not be blank")
    @Size(max = 255, message = "eventDescription max length is 255")
    String eventDescription,
    @Size(max = 128, message = "eventLocation max length is 128")
    String eventLocation,
    Boolean visibleToCustomer
) {
}
