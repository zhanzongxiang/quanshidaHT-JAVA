package com.qsd.admin.payment.controller;

import com.qsd.admin.payment.dto.RefundCallbackRequest;
import com.qsd.admin.payment.dto.WechatCallbackContext;
import com.qsd.admin.payment.dto.WechatCallbackAckResponse;
import com.qsd.admin.payment.dto.WechatPayCallbackRequest;
import com.qsd.admin.payment.service.PaymentService;
import com.qsd.admin.payment.service.WechatCallbackException;
import com.qsd.admin.payment.service.WechatPayCallbackParser;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment/callback")
public class PaymentCallbackController {
    private final PaymentService paymentService;
    private final WechatPayCallbackParser wechatPayCallbackParser;

    public PaymentCallbackController(PaymentService paymentService, WechatPayCallbackParser wechatPayCallbackParser) {
        this.paymentService = paymentService;
        this.wechatPayCallbackParser = wechatPayCallbackParser;
    }

    @PostMapping("/wechat")
    public WechatCallbackAckResponse wechatCallback(
        @org.springframework.web.bind.annotation.RequestBody String body,
        @RequestHeader("Wechatpay-Timestamp") String timestamp,
        @RequestHeader("Wechatpay-Nonce") String nonce,
        @RequestHeader("Wechatpay-Signature") String signature
    ) {
        if (isBlank(timestamp) || isBlank(nonce) || isBlank(signature)) {
            return WechatCallbackAckResponse.fail("缺少验签参数");
        }
        try {
            WechatPayCallbackRequest request = wechatPayCallbackParser.parsePaymentCallback(body);
            WechatCallbackContext context = wechatPayCallbackParser.parsePaymentCallbackContext(body, timestamp, nonce, signature);
            paymentService.validatePayCallbackMerchant(request.orderNo(), context.merchantConfig().getMchId());
            paymentService.handleWechatCallback(request);
            return WechatCallbackAckResponse.success();
        } catch (WechatCallbackException ex) {
            paymentService.recordPaymentCallbackFailure(body, ex.category(), ex.getMessage());
            return WechatCallbackAckResponse.fail(ex.retryable() ? "retry" : ex.getMessage());
        } catch (Exception ex) {
            paymentService.recordPaymentCallbackFailure(body, "callback_unknown", ex.getMessage());
            return WechatCallbackAckResponse.fail("retry");
        }
    }

    @PostMapping("/wechat-refund")
    public WechatCallbackAckResponse wechatRefundCallback(
        @org.springframework.web.bind.annotation.RequestBody String body,
        @RequestHeader("Wechatpay-Timestamp") String timestamp,
        @RequestHeader("Wechatpay-Nonce") String nonce,
        @RequestHeader("Wechatpay-Signature") String signature
    ) {
        if (isBlank(timestamp) || isBlank(nonce) || isBlank(signature)) {
            return WechatCallbackAckResponse.fail("缺少验签参数");
        }
        try {
            RefundCallbackRequest request = wechatPayCallbackParser.parseRefundCallback(body);
            WechatCallbackContext context = wechatPayCallbackParser.parseRefundCallbackContext(body, timestamp, nonce, signature);
            paymentService.validateRefundCallbackMerchant(request.refundNo(), context.merchantConfig().getMchId());
            paymentService.handleRefundCallback(request);
            return WechatCallbackAckResponse.success();
        } catch (WechatCallbackException ex) {
            paymentService.recordRefundCallbackFailure(body, ex.category(), ex.getMessage());
            return WechatCallbackAckResponse.fail(ex.retryable() ? "retry" : ex.getMessage());
        } catch (Exception ex) {
            paymentService.recordRefundCallbackFailure(body, "callback_unknown", ex.getMessage());
            return WechatCallbackAckResponse.fail("retry");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
