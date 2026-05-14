package com.qsd.admin.payment.service;

final class PaymentMerchantExceptionMessages {
    static final String CURRENT_MERCHANT_MISSING = "当前未配置可用的支付商户";
    static final String MERCHANT_NOT_FOUND = "支付商户不存在";
    static final String MERCHANT_DISABLED = "请先启用商户，再设置为当前生效商户";

    private PaymentMerchantExceptionMessages() {
    }
}
