package com.qsd.admin.payment.dto;

public record WechatCallbackAckResponse(
    String code,
    String message
) {
    public static WechatCallbackAckResponse success() {
        return new WechatCallbackAckResponse("SUCCESS", "成功");
    }

    public static WechatCallbackAckResponse fail(String message) {
        return new WechatCallbackAckResponse("FAIL", message == null || message.isBlank() ? "失败" : message);
    }
}
