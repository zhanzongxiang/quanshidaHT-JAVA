package com.qsd.admin.payment.service;

import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.payment.dto.MerchantCertificateStatusResponse;
import com.qsd.admin.payment.dto.NotifyFailureStatResponse;
import com.qsd.admin.payment.dto.PaymentOpsAlertResponse;
import com.qsd.admin.payment.dto.PaymentOpsOverviewResponse;
import com.qsd.admin.payment.dto.ReconcileDiffDetailResponse;
import com.qsd.admin.config.WechatPayProperties;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.entity.PayReconcileRecord;
import com.qsd.admin.payment.mapper.PayNotifyLogMapper;
import com.qsd.admin.payment.mapper.PayReconcileRecordMapper;
import com.qsd.admin.payment.mapper.RefundNotifyLogMapper;
import com.qsd.admin.tenant.TenantContextHolder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PaymentOpsService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PaymentMerchantService paymentMerchantService;
    private final WechatPlatformCertificateService wechatPlatformCertificateService;
    private final WechatPayProperties wechatPayProperties;
    private final PayNotifyLogMapper payNotifyLogMapper;
    private final RefundNotifyLogMapper refundNotifyLogMapper;
    private final PayReconcileRecordMapper payReconcileRecordMapper;

    public PaymentOpsService(
        PaymentMerchantService paymentMerchantService,
        WechatPlatformCertificateService wechatPlatformCertificateService,
        WechatPayProperties wechatPayProperties,
        PayNotifyLogMapper payNotifyLogMapper,
        RefundNotifyLogMapper refundNotifyLogMapper,
        PayReconcileRecordMapper payReconcileRecordMapper
    ) {
        this.paymentMerchantService = paymentMerchantService;
        this.wechatPlatformCertificateService = wechatPlatformCertificateService;
        this.wechatPayProperties = wechatPayProperties;
        this.payNotifyLogMapper = payNotifyLogMapper;
        this.refundNotifyLogMapper = refundNotifyLogMapper;
        this.payReconcileRecordMapper = payReconcileRecordMapper;
    }

    public PaymentOpsOverviewResponse getOverview() {
        Long tenantId = TenantContextHolder.requireTenantId();
        PayMerchantConfig merchant = paymentMerchantService.requireCurrentMerchant();
        List<NotifyFailureStatResponse> paymentFailures = payNotifyLogMapper.selectFailureStats(tenantId);
        List<NotifyFailureStatResponse> refundFailures = refundNotifyLogMapper.selectFailureStats(tenantId);
        List<PayReconcileRecord> reconcileRecords = payReconcileRecordMapper.selectListByChannel(tenantId, null);
        return new PaymentOpsOverviewResponse(
            buildCertificateStatus(merchant, merchant.getPlatformCertificatePath()),
            paymentFailures,
            refundFailures,
            buildAlerts(merchant, paymentFailures, refundFailures, reconcileRecords.isEmpty() ? null : reconcileRecords.get(0))
        );
    }

    public MerchantCertificateStatusResponse refreshCurrentMerchantCertificate() {
        String path = wechatPlatformCertificateService.refreshCurrentMerchantCertificate();
        PayMerchantConfig merchant = paymentMerchantService.requireCurrentMerchant();
        return buildCertificateStatus(merchant, path);
    }

    public ReconcileDiffDetailResponse getReconcileDiffDetail(Long id) {
        PayReconcileRecord record = payReconcileRecordMapper.selectActiveById(TenantContextHolder.requireTenantId(), id);
        if (record == null) {
            throw new NotFoundException("对账记录不存在");
        }

        String summary = record.getSummary() == null ? "" : record.getSummary();
        return new ReconcileDiffDetailResponse(
            record.getId(),
            record.getReconcileDate() == null ? null : record.getReconcileDate().toString(),
            record.getChannel(),
            record.getReconcileStatus(),
            record.getDiffCount(),
            parseDiffItems(summary),
            summary
        );
    }

    private MerchantCertificateStatusResponse buildCertificateStatus(PayMerchantConfig merchant, String certificatePath) {
        return new MerchantCertificateStatusResponse(
            merchant.getId(),
            merchant.getMerchantName(),
            merchant.getMchId(),
            certificatePath,
            true,
            merchant.getUpdatedAt() == null ? null : DATE_TIME_FORMATTER.format(merchant.getUpdatedAt())
        );
    }

    private List<PaymentOpsAlertResponse> buildAlerts(
        PayMerchantConfig merchant,
        List<NotifyFailureStatResponse> paymentFailures,
        List<NotifyFailureStatResponse> refundFailures,
        PayReconcileRecord latestReconcile
    ) {
        List<PaymentOpsAlertResponse> alerts = new ArrayList<>();

        List<String> configurationIssues = paymentMerchantService.buildConfigurationIssuesForOps(merchant);
        if (!configurationIssues.isEmpty()) {
            alerts.add(new PaymentOpsAlertResponse(
                "critical",
                "merchant_config_incomplete",
                "Active merchant configuration is incomplete",
                "Missing or invalid items: " + String.join(", ", configurationIssues),
                "Complete the active merchant config before real-payment cutover"
            ));
        }

        if (!wechatPayProperties.mockEnabled() && !wechatPayProperties.autoRefreshPlatformCertificates()) {
            alerts.add(new PaymentOpsAlertResponse(
                "warning",
                "certificate_auto_refresh_disabled",
                "Platform certificate auto-refresh is disabled",
                "Real-payment mode is active but platform certificate auto-refresh is turned off.",
                "Enable certificate auto-refresh or schedule manual refresh ownership"
            ));
        }

        int paymentFailureCount = sumFailures(paymentFailures);
        if (paymentFailureCount > 0) {
            alerts.add(new PaymentOpsAlertResponse(
                paymentFailureCount >= 5 ? "critical" : "warning",
                "payment_callback_failures_present",
                "Payment callback failures detected",
                "Payment callback failures in recent logs: " + paymentFailureCount,
                "Inspect callback logs, replay failed items, and verify signature/decryption paths"
            ));
        }

        int refundFailureCount = sumFailures(refundFailures);
        if (refundFailureCount > 0) {
            alerts.add(new PaymentOpsAlertResponse(
                refundFailureCount >= 5 ? "critical" : "warning",
                "refund_callback_failures_present",
                "Refund callback failures detected",
                "Refund callback failures in recent logs: " + refundFailureCount,
                "Inspect refund callback logs and verify refund notification handling"
            ));
        }

        if (latestReconcile == null) {
            alerts.add(new PaymentOpsAlertResponse(
                "warning",
                "reconcile_missing",
                "No reconcile record is available",
                "No reconcile record has been generated for the current tenant yet.",
                "Create and verify a reconcile run for the active merchant"
            ));
        } else if ((latestReconcile.getDiffCount() != null && latestReconcile.getDiffCount() > 0)
            || isWarningStatus(latestReconcile.getReconcileStatus())) {
            alerts.add(new PaymentOpsAlertResponse(
                isCriticalStatus(latestReconcile.getReconcileStatus()) ? "critical" : "warning",
                "reconcile_attention_required",
                "Latest reconcile result requires attention",
                "Latest reconcile status: " + safe(latestReconcile.getReconcileStatus())
                    + ", diff count: " + (latestReconcile.getDiffCount() == null ? 0 : latestReconcile.getDiffCount()),
                "Open reconcile details and resolve diff items before production cutover"
            ));
        }

        if (alerts.isEmpty()) {
            alerts.add(new PaymentOpsAlertResponse(
                "info",
                "ops_healthy",
                "No immediate payment-ops alert",
                "Current merchant config, callback failures, and latest reconcile record do not require urgent action.",
                "Continue routine monitoring and scheduled verification"
            ));
        }

        return alerts;
    }

    private List<String> parseDiffItems(String summary) {
        int index = summary.indexOf("diffs=");
        if (index < 0) {
            return List.of();
        }

        String diffPart = summary.substring(index + "diffs=".length());
        return Arrays.stream(diffPart.split("\\|"))
            .map(String::trim)
            .filter(item -> !item.isEmpty())
            .toList();
    }

    private int sumFailures(List<NotifyFailureStatResponse> items) {
        return items.stream()
            .map(NotifyFailureStatResponse::count)
            .filter(count -> count != null && count > 0)
            .mapToInt(Integer::intValue)
            .sum();
    }

    private boolean isWarningStatus(String value) {
        String normalized = safe(value).toLowerCase();
        return normalized.contains("warning") || normalized.contains("failed") || normalized.contains("error");
    }

    private boolean isCriticalStatus(String value) {
        String normalized = safe(value).toLowerCase();
        return normalized.contains("failed") || normalized.contains("error");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
