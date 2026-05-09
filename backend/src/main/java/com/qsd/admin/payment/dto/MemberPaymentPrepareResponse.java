package com.qsd.admin.payment.dto;

public record MemberPaymentPrepareResponse(
    Long payOrderId,
    String orderNo,
    String status,
    Long merchantConfigId,
    String merchantName,
    String appId,
    String timeStamp,
    String nonceStr,
    String packageValue,
    String signType,
    String paySign
) {
}
