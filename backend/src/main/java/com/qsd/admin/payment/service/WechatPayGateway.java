package com.qsd.admin.payment.service;

import com.qsd.admin.payment.dto.WechatCodeSessionResponse;
import com.qsd.admin.payment.dto.WechatMiniProgramPayParams;
import com.qsd.admin.payment.dto.WechatReconcileDownloadResult;
import com.qsd.admin.payment.dto.WechatRefundResult;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.entity.PayOrder;
import com.qsd.admin.payment.entity.RefundOrder;

import java.time.LocalDate;

public interface WechatPayGateway {
    WechatCodeSessionResponse exchangeCode(String code, PayMerchantConfig merchantConfig);

    WechatMiniProgramPayParams prepareMiniProgramPayment(PayOrder order, String openid, PayMerchantConfig merchantConfig);

    WechatRefundResult createRefund(PayOrder order, RefundOrder refundOrder, PayMerchantConfig merchantConfig);

    WechatReconcileDownloadResult downloadTradeBill(LocalDate billDate, PayMerchantConfig merchantConfig);
}
