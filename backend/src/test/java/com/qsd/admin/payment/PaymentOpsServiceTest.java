package com.qsd.admin.payment;

import com.qsd.admin.config.WechatPayProperties;
import com.qsd.admin.payment.dto.NotifyFailureStatResponse;
import com.qsd.admin.payment.dto.PaymentOpsOverviewResponse;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.entity.PayReconcileRecord;
import com.qsd.admin.payment.mapper.PayNotifyLogMapper;
import com.qsd.admin.payment.mapper.PayReconcileRecordMapper;
import com.qsd.admin.payment.mapper.RefundNotifyLogMapper;
import com.qsd.admin.payment.service.PaymentMerchantService;
import com.qsd.admin.payment.service.PaymentOpsService;
import com.qsd.admin.payment.service.WechatPlatformCertificateService;
import com.qsd.admin.tenant.TenantContext;
import com.qsd.admin.tenant.TenantContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentOpsServiceTest {
    @Mock
    private PaymentMerchantService paymentMerchantService;

    @Mock
    private WechatPlatformCertificateService wechatPlatformCertificateService;

    @Mock
    private PayNotifyLogMapper payNotifyLogMapper;

    @Mock
    private RefundNotifyLogMapper refundNotifyLogMapper;

    @Mock
    private PayReconcileRecordMapper payReconcileRecordMapper;

    private PaymentOpsService paymentOpsService;

    @BeforeEach
    void setUp() {
        TenantContextHolder.set(new TenantContext(1L, "default", "Default Tenant"));
        paymentOpsService = new PaymentOpsService(
            paymentMerchantService,
            wechatPlatformCertificateService,
            new WechatPayProperties(
                false,
                false,
                "certs",
                6,
                "Acme Merchant",
                "wx-app-001",
                "secret",
                "mch-001",
                "https://pay.example.com/api/payment/callback/wechat",
                "12345678901234567890123456789012",
                "E:/secure/apiclient_key.pem",
                "serial-001",
                "E:/secure/platform.pem"
            ),
            payNotifyLogMapper,
            refundNotifyLogMapper,
            payReconcileRecordMapper
        );
    }

    private PaymentOpsService createService(boolean mockEnabled, boolean autoRefreshEnabled) {
        return new PaymentOpsService(
            paymentMerchantService,
            wechatPlatformCertificateService,
            new WechatPayProperties(
                mockEnabled,
                autoRefreshEnabled,
                "certs",
                6,
                "Acme Merchant",
                "wx-app-001",
                "secret",
                "mch-001",
                "https://pay.example.com/api/payment/callback/wechat",
                "12345678901234567890123456789012",
                "E:/secure/apiclient_key.pem",
                "serial-001",
                "E:/secure/platform.pem"
            ),
            payNotifyLogMapper,
            refundNotifyLogMapper,
            payReconcileRecordMapper
        );
    }

    @Test
    void shouldBuildCriticalAndWarningOpsAlerts() {
        PayMerchantConfig merchant = new PayMerchantConfig();
        merchant.setId(1L);
        merchant.setMerchantName("Acme Merchant");
        merchant.setMchId("mch-001");
        merchant.setAppId("wx-app-001");
        merchant.setNotifyUrl("https://pay.example.com/api/payment/callback/wechat");
        merchant.setApiV3Key("short");
        merchant.setPrivateKeyPath("E:/secure/apiclient_key.pem");
        merchant.setMerchantSerialNo("");
        merchant.setPlatformCertificatePath("");
        merchant.setUpdatedAt(LocalDateTime.of(2026, 6, 11, 8, 0));

        PayReconcileRecord reconcileRecord = new PayReconcileRecord();
        reconcileRecord.setId(8L);
        reconcileRecord.setReconcileDate(LocalDate.of(2026, 6, 10));
        reconcileRecord.setReconcileStatus("warning");
        reconcileRecord.setDiffCount(2);

        when(paymentMerchantService.requireCurrentMerchant()).thenReturn(merchant);
        when(paymentMerchantService.buildConfigurationIssuesForOps(merchant)).thenReturn(
            List.of("invalid_api_v3_key_length", "incomplete_certificate_bundle")
        );
        when(payNotifyLogMapper.selectFailureStats(1L)).thenReturn(List.of(
            new NotifyFailureStatResponse("signature_verify_failed", 6, "2026-06-11 07:55:00")
        ));
        when(refundNotifyLogMapper.selectFailureStats(1L)).thenReturn(List.of(
            new NotifyFailureStatResponse("resource_decrypt_failed", 1, "2026-06-11 07:56:00")
        ));
        when(payReconcileRecordMapper.selectListByChannel(1L, null)).thenReturn(List.of(reconcileRecord));

        PaymentOpsOverviewResponse response = paymentOpsService.getOverview();

        assertEquals(5, response.alerts().size());
        assertTrue(response.alerts().stream().anyMatch(item -> item.rule().equals("merchant_config_incomplete") && item.severity().equals("critical")));
        assertTrue(response.alerts().stream().anyMatch(item -> item.rule().equals("certificate_auto_refresh_disabled") && item.severity().equals("warning")));
        assertTrue(response.alerts().stream().anyMatch(item -> item.rule().equals("payment_callback_failures_present") && item.severity().equals("critical")));
        assertTrue(response.alerts().stream().anyMatch(item -> item.rule().equals("refund_callback_failures_present") && item.severity().equals("warning")));
        assertTrue(response.alerts().stream().anyMatch(item -> item.rule().equals("reconcile_attention_required") && item.severity().equals("warning")));
    }

    @Test
    void shouldReturnHealthyInfoAlertWhenNoIssueExists() {
        paymentOpsService = createService(false, true);

        PayMerchantConfig merchant = new PayMerchantConfig();
        merchant.setId(1L);
        merchant.setMerchantName("Acme Merchant");
        merchant.setMchId("mch-001");
        merchant.setPlatformCertificatePath("certs/acme.pem");
        merchant.setUpdatedAt(LocalDateTime.of(2026, 6, 11, 8, 0));

        PayReconcileRecord reconcileRecord = new PayReconcileRecord();
        reconcileRecord.setId(9L);
        reconcileRecord.setReconcileDate(LocalDate.of(2026, 6, 10));
        reconcileRecord.setReconcileStatus("success");
        reconcileRecord.setDiffCount(0);

        when(paymentMerchantService.requireCurrentMerchant()).thenReturn(merchant);
        when(paymentMerchantService.buildConfigurationIssuesForOps(merchant)).thenReturn(List.of());
        when(payNotifyLogMapper.selectFailureStats(1L)).thenReturn(List.of());
        when(refundNotifyLogMapper.selectFailureStats(1L)).thenReturn(List.of());
        when(payReconcileRecordMapper.selectListByChannel(1L, null)).thenReturn(List.of(reconcileRecord));

        PaymentOpsOverviewResponse response = paymentOpsService.getOverview();

        assertEquals(1, response.alerts().size());
        assertEquals("ops_healthy", response.alerts().get(0).rule());
        assertEquals("info", response.alerts().get(0).severity());
    }
}
