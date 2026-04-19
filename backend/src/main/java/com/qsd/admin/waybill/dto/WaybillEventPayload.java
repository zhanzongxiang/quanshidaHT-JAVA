package com.qsd.admin.waybill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WaybillEventPayload(
    Long legId,
    Integer sortNo,
    @NotBlank(message = "事件时间不能为空")
    String eventTime,
    @NotBlank(message = "事件状态不能为空")
    @Size(max = 64, message = "事件状态长度不能超过 64 个字符")
    String eventStatus,
    @NotBlank(message = "事件描述不能为空")
    @Size(max = 255, message = "事件描述长度不能超过 255 个字符")
    String eventDescription,
    @Size(max = 128, message = "事件位置长度不能超过 128 个字符")
    String eventLocation,
    Boolean visibleToCustomer
) {
}
