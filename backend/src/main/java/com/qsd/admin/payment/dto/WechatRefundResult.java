package com.qsd.admin.payment.dto;

public record WechatRefundResult(
    String externalRefundNo,
    String status,
    String rawResponse
) {
}
