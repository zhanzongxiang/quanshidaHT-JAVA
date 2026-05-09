package com.qsd.admin.payment;

import com.qsd.admin.payment.controller.PaymentAdminController;
import com.qsd.admin.payment.dto.MerchantCertificateStatusResponse;
import com.qsd.admin.payment.dto.NotifyFailureStatResponse;
import com.qsd.admin.payment.dto.NotifyReplayResponse;
import com.qsd.admin.payment.dto.PaymentAdminSummaryResponse;
import com.qsd.admin.payment.dto.PaymentOpsOverviewResponse;
import com.qsd.admin.payment.dto.ReconcileDiffDetailResponse;
import com.qsd.admin.payment.dto.RefundOrderResponse;
import com.qsd.admin.payment.service.PaymentMerchantService;
import com.qsd.admin.payment.service.PaymentNotifyReplayService;
import com.qsd.admin.payment.service.PaymentOpsService;
import com.qsd.admin.payment.service.PaymentService;
import com.qsd.admin.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @MockBean
    private PaymentMerchantService paymentMerchantService;

    @MockBean
    private PaymentOpsService paymentOpsService;

    @MockBean
    private PaymentNotifyReplayService paymentNotifyReplayService;

    @Test
    void shouldReturnPaymentList() throws Exception {
        when(paymentService.listAdminPayOrders(nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(List.of(
            new PaymentAdminSummaryResponse(
                1L,
                "PO202605080001",
                2L,
                9L,
                "Acme Merchant",
                "mch-001",
                "wx-app-001",
                "13800138000",
                3L,
                "WB202605080001",
                "waybill",
                "mini_program",
                "wechat_pay",
                "CNY",
                new BigDecimal("99.00"),
                BigDecimal.ZERO,
                "pending",
                "",
                null,
                "2026-05-08 09:00:00"
            )
        ));

        mockMvc.perform(get("/api/admin/payments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[0].orderNo").value("PO202605080001"))
            .andExpect(jsonPath("$.data[0].merchantName").value("Acme Merchant"))
            .andExpect(jsonPath("$.data[0].channel").value("wechat_pay"));
    }

    @Test
    void shouldReturnOpsOverview() throws Exception {
        when(paymentOpsService.getOverview()).thenReturn(new PaymentOpsOverviewResponse(
            new MerchantCertificateStatusResponse(
                1L,
                "Acme Merchant",
                "mch-001",
                "certs/acme.pem",
                true,
                "2026-05-09 10:00:00"
            ),
            List.of(new NotifyFailureStatResponse("signature_verify_failed", 2, "2026-05-09 09:00:00")),
            List.of(new NotifyFailureStatResponse("resource_decrypt_failed", 1, "2026-05-09 09:30:00"))
        ));

        mockMvc.perform(get("/api/admin/payments/ops/overview"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.currentMerchantCertificate.merchantName").value("Acme Merchant"))
            .andExpect(jsonPath("$.data.paymentNotifyFailures[0].category").value("signature_verify_failed"))
            .andExpect(jsonPath("$.data.refundNotifyFailures[0].count").value(1));
    }

    @Test
    void shouldRefreshCertificate() throws Exception {
        when(paymentOpsService.refreshCurrentMerchantCertificate()).thenReturn(new MerchantCertificateStatusResponse(
            1L,
            "Acme Merchant",
            "mch-001",
            "certs/acme-new.pem",
            true,
            "2026-05-09 10:30:00"
        ));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/payments/ops/certificate-refresh"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.certificatePath").value("certs/acme-new.pem"));
    }

    @Test
    void shouldReturnReconcileDiffDetail() throws Exception {
        when(paymentOpsService.getReconcileDiffDetail(8L)).thenReturn(new ReconcileDiffDetailResponse(
            8L,
            "2026-05-08",
            "wechat_pay",
            "warning",
            2,
            List.of("LOCAL_MISSING:PO202605080001", "REMOTE_AMOUNT_MISMATCH:PO202605080009"),
            "bill downloaded, diffs=LOCAL_MISSING:PO202605080001|REMOTE_AMOUNT_MISMATCH:PO202605080009"
        ));

        mockMvc.perform(get("/api/admin/payments/reconcile-records/8/diffs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.id").value(8))
            .andExpect(jsonPath("$.data.diffItems[1]").value("REMOTE_AMOUNT_MISMATCH:PO202605080009"));
    }

    @Test
    void shouldReplayPaymentNotify() throws Exception {
        when(paymentNotifyReplayService.replayPaymentNotify(11L)).thenReturn(
            new NotifyReplayResponse(11L, "payment", "replayed", "payment callback replayed successfully")
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/payments/notify-logs/11/replay"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.replayType").value("payment"))
            .andExpect(jsonPath("$.data.replayStatus").value("replayed"));
    }

    @Test
    void shouldReplayRefundNotify() throws Exception {
        when(paymentNotifyReplayService.replayRefundNotify(12L)).thenReturn(
            new NotifyReplayResponse(12L, "refund", "replayed", "refund callback replayed successfully")
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/payments/refund-notify-logs/12/replay"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.replayType").value("refund"))
            .andExpect(jsonPath("$.data.sourceLogId").value(12));
    }

    @Test
    void shouldRetryRefund() throws Exception {
        when(paymentService.retryRefund(21L)).thenReturn(
            new RefundOrderResponse(
                22L,
                "RF202605090001",
                new BigDecimal("19.90"),
                "processing",
                "customer retry",
                "mock-refund-RF202605090001",
                null,
                "2026-05-09 10:10:00"
            )
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/payments/refunds/21/retry"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.refundNo").value("RF202605090001"))
            .andExpect(jsonPath("$.data.status").value("processing"));
    }
}
