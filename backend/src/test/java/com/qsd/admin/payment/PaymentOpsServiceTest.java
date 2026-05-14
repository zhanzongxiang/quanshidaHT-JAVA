package com.qsd.admin.payment;

import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.payment.dto.NotifyFailureStatResponse;
import com.qsd.admin.payment.dto.PaymentOpsOverviewResponse;
import com.qsd.admin.payment.dto.ReconcileDiffDetailResponse;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.entity.PayReconcileRecord;
import com.qsd.admin.payment.mapper.PayNotifyLogMapper;
import com.qsd.admin.payment.mapper.PayReconcileRecordMapper;
import com.qsd.admin.payment.mapper.RefundNotifyLogMapper;
import com.qsd.admin.payment.service.PaymentMerchantService;
import com.qsd.admin.payment.service.PaymentOpsService;
import com.qsd.admin.payment.service.WechatPlatformCertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        paymentOpsService = new PaymentOpsService(
            paymentMerchantService,
            wechatPlatformCertificateService,
            payNotifyLogMapper,
            refundNotifyLogMapper,
            payReconcileRecordMapper
        );
    }

    @Test
    void shouldReturnOpsOverview() {
        PayMerchantConfig merchant = new PayMerchantConfig();
        merchant.setId(8L);
        merchant.setMerchantName("Acme Merchant");
        merchant.setMchId("mch-001");
        merchant.setPlatformCertificatePath("certs/acme.pem");
        merchant.setUpdatedAt(LocalDateTime.of(2026, 5, 13, 10, 0, 0));

        when(paymentMerchantService.requireCurrentMerchant()).thenReturn(merchant);
        when(payNotifyLogMapper.selectFailureStats()).thenReturn(
            List.of(new NotifyFailureStatResponse("signature_verify_failed", 2, "2026-05-13 09:00:00"))
        );
        when(refundNotifyLogMapper.selectFailureStats()).thenReturn(
            List.of(new NotifyFailureStatResponse("resource_decrypt_failed", 1, "2026-05-13 09:30:00"))
        );

        PaymentOpsOverviewResponse response = paymentOpsService.getOverview();

        assertEquals("Acme Merchant", response.currentMerchantCertificate().merchantName());
        assertEquals("certs/acme.pem", response.currentMerchantCertificate().certificatePath());
        assertEquals("signature_verify_failed", response.paymentNotifyFailures().get(0).category());
        assertEquals(1, response.refundNotifyFailures().get(0).count());
    }

    @Test
    void shouldReturnRefreshedCertificateStatus() {
        PayMerchantConfig merchant = new PayMerchantConfig();
        merchant.setId(9L);
        merchant.setMerchantName("Acme Merchant");
        merchant.setMchId("mch-002");
        merchant.setUpdatedAt(LocalDateTime.of(2026, 5, 13, 11, 30, 0));

        when(wechatPlatformCertificateService.refreshCurrentMerchantCertificate()).thenReturn("certs/acme-new.pem");
        when(paymentMerchantService.requireCurrentMerchant()).thenReturn(merchant);

        var response = paymentOpsService.refreshCurrentMerchantCertificate();

        assertEquals(9L, response.merchantConfigId());
        assertEquals("certs/acme-new.pem", response.certificatePath());
        assertEquals("2026-05-13 11:30:00", response.lastUpdatedAt());
    }

    @Test
    void shouldParseReconcileDiffItems() {
        PayReconcileRecord record = new PayReconcileRecord();
        record.setId(15L);
        record.setReconcileDate(LocalDate.of(2026, 5, 12));
        record.setChannel("wechat_pay");
        record.setReconcileStatus("warning");
        record.setDiffCount(2);
        record.setSummary("bill downloaded, diffs=LOCAL_MISSING:PO1|REMOTE_AMOUNT_MISMATCH:PO2|");

        when(payReconcileRecordMapper.selectById(15L)).thenReturn(record);

        ReconcileDiffDetailResponse response = paymentOpsService.getReconcileDiffDetail(15L);

        assertEquals(15L, response.id());
        assertEquals(2, response.diffItems().size());
        assertEquals("LOCAL_MISSING:PO1", response.diffItems().get(0));
        assertEquals("REMOTE_AMOUNT_MISMATCH:PO2", response.diffItems().get(1));
    }

    @Test
    void shouldRejectMissingReconcileRecord() {
        when(payReconcileRecordMapper.selectById(16L)).thenReturn(null);

        NotFoundException ex = assertThrows(
            NotFoundException.class,
            () -> paymentOpsService.getReconcileDiffDetail(16L)
        );

        assertEquals("对账记录不存在", ex.getMessage());
    }
}
