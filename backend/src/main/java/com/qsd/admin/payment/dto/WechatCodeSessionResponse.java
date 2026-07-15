package com.qsd.admin.payment.dto;

public record WechatCodeSessionResponse(
    String openid,
    String unionid,
    String sessionKey
) {
}
