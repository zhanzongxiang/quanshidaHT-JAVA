package com.qsd.admin.payment.service;

import com.qsd.admin.config.WechatPayProperties;
import com.qsd.admin.payment.dto.WechatCodeSessionResponse;
import com.qsd.admin.payment.dto.WechatMiniProgramPayParams;
import com.qsd.admin.payment.dto.WechatReconcileDownloadResult;
import com.qsd.admin.payment.dto.WechatRefundResult;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.entity.PayOrder;
import com.qsd.admin.payment.entity.RefundOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "app.wechat-pay", name = "mock-enabled", havingValue = "true", matchIfMissing = true)
public class MockWechatPayGateway implements WechatPayGateway {
    private final WechatPayProperties wechatPayProperties;

    public MockWechatPayGateway(WechatPayProperties wechatPayProperties) {
        this.wechatPayProperties = wechatPayProperties;
    }

    @Override
    public WechatCodeSessionResponse exchangeCode(String code, PayMerchantConfig merchantConfig) {
        String normalized = code.trim();
        String openid = "mock-openid-" + normalized;
        String unionid = "mock-unionid-" + normalized;
        return new WechatCodeSessionResponse(openid, unionid, "mock-session-" + normalized);
    }

    @Override
    public WechatMiniProgramPayParams prepareMiniProgramPayment(PayOrder order, String openid, PayMerchantConfig merchantConfig) {
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String timeStamp = String.valueOf(Instant.now().getEpochSecond());
        String prepayId = "mock-prepay-" + order.getOrderNo();
        return new WechatMiniProgramPayParams(
            merchantConfig == null ? wechatPayProperties.appId() : merchantConfig.getAppId(),
            timeStamp,
            nonce,
            "prepay_id=" + prepayId,
            "RSA",
            "mock-sign-" + order.getOrderNo(),
            prepayId,
            "mock-txn-" + order.getOrderNo()
        );
    }

    @Override
    public WechatRefundResult createRefund(PayOrder order, RefundOrder refundOrder, PayMerchantConfig merchantConfig) {
        return new WechatRefundResult(
            "mock-refund-" + refundOrder.getRefundNo(),
            "PROCESSING",
            "{\"mock\":true}"
        );
    }

    @Override
    public WechatReconcileDownloadResult downloadTradeBill(LocalDate billDate, PayMerchantConfig merchantConfig) {
        String content = """
            `交易时间`,`公众账号ID`,`商户号`,`商户订单号`,`微信支付订单号`,`订单金额`,`申请退款金额`,`退款金额`,`订单状态`
            `2026-05-08 12:00:00`,`%s`,`%s`,`PO-MOCK-001`,`wx-mock-001`,`100`,`0`,`0`,`SUCCESS`
            """.formatted(merchantConfig.getAppId(), merchantConfig.getMchId());
        return new WechatReconcileDownloadResult(billDate.toString(), content, "mock://trade-bill");
    }
}
