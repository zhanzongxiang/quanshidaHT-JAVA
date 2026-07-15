package com.qsd.admin.payment.dto;

public record WechatReconcileDownloadResult(
    String billDate,
    String content,
    String downloadUrl
) {
}
