package com.qsd.admin.payment.dto;

public record WechatMiniProgramPayParams(
    String appId,
    String timeStamp,
    String nonceStr,
    String packageValue,
    String signType,
    String paySign,
    String prepayId,
    String transactionNo
) {
}
