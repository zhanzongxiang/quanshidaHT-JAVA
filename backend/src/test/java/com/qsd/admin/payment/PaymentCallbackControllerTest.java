package com.qsd.admin.payment;

import com.qsd.admin.payment.controller.PaymentCallbackController;
import com.qsd.admin.payment.dto.RefundCallbackRequest;
import com.qsd.admin.payment.dto.WechatCallbackContext;
import com.qsd.admin.payment.dto.WechatPayCallbackRequest;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.service.PaymentMerchantService;
import com.qsd.admin.payment.service.PaymentService;
import com.qsd.admin.payment.service.WechatCallbackException;
import com.qsd.admin.payment.service.WechatPayCallbackParser;
import com.qsd.admin.security.JwtTokenService;
import com.qsd.admin.tenant.TenantContext;
import com.qsd.admin.tenant.service.TenantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentCallbackController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentCallbackControllerTest {
    private static final TenantContext TENANT_CONTEXT = new TenantContext(1L, "default", "Default Tenant");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @MockBean
    private PaymentMerchantService paymentMerchantService;

    @MockBean
    private TenantService tenantService;

    @MockBean
    private WechatPayCallbackParser wechatPayCallbackParser;

    @Test
    void shouldAcceptWechatCallback() throws Exception {
        doNothing().when(paymentService).handleWechatCallback(any());
        doNothing().when(paymentService).validatePayCallbackMerchant(any(), any());
        when(wechatPayCallbackParser.parsePaymentCallback(any())).thenReturn(
            new WechatPayCallbackRequest("PO202605080001", "wx-txn-001", "SUCCESS", "{\"event\":\"pay\"}")
        );
        when(wechatPayCallbackParser.parsePaymentCallbackContext(any(), any(), any(), any()))
            .thenReturn(buildCallbackContext());

        mockMvc.perform(post("/api/payment/callback/wechat")
                .header("Wechatpay-Timestamp", "1710000000")
                .header("Wechatpay-Nonce", "nonce-001")
                .header("Wechatpay-Signature", "signature-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "orderNo": "PO202605080001",
                      "transactionNo": "wx-txn-001",
                      "status": "SUCCESS",
                      "payload": "{\\\"event\\\":\\\"pay\\\"}"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("成功"));
    }

    @Test
    void shouldAcceptWechatRefundCallback() throws Exception {
        doNothing().when(paymentService).handleRefundCallback(any());
        doNothing().when(paymentService).validateRefundCallbackMerchant(any(), any());
        when(wechatPayCallbackParser.parseRefundCallback(any())).thenReturn(
            new RefundCallbackRequest("RF202605080001", "SUCCESS", "wx-rf-001", "{\"event\":\"refund\"}")
        );
        when(wechatPayCallbackParser.parseRefundCallbackContext(any(), any(), any(), any()))
            .thenReturn(buildCallbackContext());

        mockMvc.perform(post("/api/payment/callback/wechat-refund")
                .header("Wechatpay-Timestamp", "1710000000")
                .header("Wechatpay-Nonce", "nonce-001")
                .header("Wechatpay-Signature", "signature-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "refundNo": "RF202605080001",
                      "status": "SUCCESS",
                      "externalRefundNo": "wx-rf-001",
                      "payload": "{\\\"event\\\":\\\"refund\\\"}"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("成功"));
    }

    @Test
    void shouldReturnBusinessFailureMessageForNonRetryableWechatCallbackError() throws Exception {
        when(wechatPayCallbackParser.parsePaymentCallback(any())).thenReturn(
            new WechatPayCallbackRequest("PO202605080001", "wx-txn-001", "SUCCESS", "{\"event\":\"pay\"}")
        );
        when(wechatPayCallbackParser.parsePaymentCallbackContext(any(), any(), any(), any()))
            .thenReturn(buildCallbackContext());
        doThrow(new WechatCallbackException("merchant_mismatch", "merchant mismatch", false))
            .when(paymentService).validatePayCallbackMerchant(any(), any());

        mockMvc.perform(post("/api/payment/callback/wechat")
                .header("Wechatpay-Timestamp", "1710000000")
                .header("Wechatpay-Nonce", "nonce-001")
                .header("Wechatpay-Signature", "signature-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "orderNo": "PO202605080001",
                      "transactionNo": "wx-txn-001",
                      "status": "SUCCESS",
                      "payload": "{\\\"event\\\":\\\"pay\\\"}"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("FAIL"))
            .andExpect(jsonPath("$.message").value("merchant mismatch"));
    }

    @Test
    void shouldReturnRetryForUnexpectedRefundCallbackError() throws Exception {
        when(wechatPayCallbackParser.parseRefundCallback(any())).thenReturn(
            new RefundCallbackRequest("RF202605080001", "SUCCESS", "wx-rf-001", "{\"event\":\"refund\"}")
        );
        when(wechatPayCallbackParser.parseRefundCallbackContext(any(), any(), any(), any()))
            .thenReturn(buildCallbackContext());
        doThrow(new RuntimeException("boom")).when(paymentService).handleRefundCallback(any());

        mockMvc.perform(post("/api/payment/callback/wechat-refund")
                .header("Wechatpay-Timestamp", "1710000000")
                .header("Wechatpay-Nonce", "nonce-001")
                .header("Wechatpay-Signature", "signature-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "refundNo": "RF202605080001",
                      "status": "SUCCESS",
                      "externalRefundNo": "wx-rf-001",
                      "payload": "{\\\"event\\\":\\\"refund\\\"}"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("FAIL"))
            .andExpect(jsonPath("$.message").value("retry"));
    }

    private WechatCallbackContext buildCallbackContext() {
        PayMerchantConfig merchantConfig = new PayMerchantConfig();
        merchantConfig.setTenantId(TENANT_CONTEXT.tenantId());
        merchantConfig.setMchId("mch-001");
        return new WechatCallbackContext(merchantConfig, Map.of(), TENANT_CONTEXT);
    }
}
