package com.qsd.admin.payment;

import com.qsd.admin.payment.controller.PaymentCallbackController;
import com.qsd.admin.payment.dto.RefundCallbackRequest;
import com.qsd.admin.payment.dto.WechatPayCallbackRequest;
import com.qsd.admin.payment.service.PaymentMerchantService;
import com.qsd.admin.payment.service.PaymentService;
import com.qsd.admin.payment.service.WechatPayCallbackParser;
import com.qsd.admin.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentCallbackController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentCallbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @MockBean
    private PaymentMerchantService paymentMerchantService;

    @MockBean
    private WechatPayCallbackParser wechatPayCallbackParser;

    @Test
    void shouldAcceptWechatCallback() throws Exception {
        doNothing().when(paymentService).handleWechatCallback(any());
        doNothing().when(paymentService).validatePayCallbackMerchant(any(), any());
        when(wechatPayCallbackParser.parsePaymentCallback(any())).thenReturn(
            new WechatPayCallbackRequest("PO202605080001", "wx-txn-001", "SUCCESS", "{\"event\":\"pay\"}")
        );

        mockMvc.perform(post("/api/payment/callback/wechat")
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
            .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    void shouldAcceptWechatRefundCallback() throws Exception {
        doNothing().when(paymentService).handleRefundCallback(any());
        doNothing().when(paymentService).validateRefundCallbackMerchant(any(), any());
        when(wechatPayCallbackParser.parseRefundCallback(any())).thenReturn(
            new RefundCallbackRequest("RF202605080001", "SUCCESS", "wx-rf-001", "{\"event\":\"refund\"}")
        );

        mockMvc.perform(post("/api/payment/callback/wechat-refund")
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
            .andExpect(jsonPath("$.code").value("SUCCESS"));
    }
}
