package com.qsd.admin.payment.service;

import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.payment.dto.MerchantCertificateStatusResponse;
import com.qsd.admin.payment.dto.PaymentOpsOverviewResponse;
import com.qsd.admin.payment.dto.ReconcileDiffDetailResponse;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.entity.PayReconcileRecord;
import com.qsd.admin.payment.mapper.PayNotifyLogMapper;
import com.qsd.admin.payment.mapper.PayReconcileRecordMapper;
import com.qsd.admin.payment.mapper.RefundNotifyLogMapper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class PaymentOpsService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PaymentMerchantService paymentMerchantService;
    private final WechatPlatformCertificateService wechatPlatformCertificateService;
    private final PayNotifyLogMapper payNotifyLogMapper;
    private final RefundNotifyLogMapper refundNotifyLogMapper;
    private final PayReconcileRecordMapper payReconcileRecordMapper;

    public PaymentOpsService(
        PaymentMerchantService paymentMerchantService,
        WechatPlatformCertificateService wechatPlatformCertificateService,
        PayNotifyLogMapper payNotifyLogMapper,
        RefundNotifyLogMapper refundNotifyLogMapper,
        PayReconcileRecordMapper payReconcileRecordMapper
    ) {
        this.paymentMerchantService = paymentMerchantService;
        this.wechatPlatformCertificateService = wechatPlatformCertificateService;
        this.payNotifyLogMapper = payNotifyLogMapper;
        this.refundNotifyLogMapper = refundNotifyLogMapper;
        this.payReconcileRecordMapper = payReconcileRecordMapper;
    }

    public PaymentOpsOverviewResponse getOverview() {
        PayMerchantConfig merchant = paymentMerchantService.requireCurrentMerchant();
        return new PaymentOpsOverviewResponse(
            buildCertificateStatus(merchant, merchant.getPlatformCertificatePath()),
            payNotifyLogMapper.selectFailureStats(),
            refundNotifyLogMapper.selectFailureStats()
        );
    }

    public MerchantCertificateStatusResponse refreshCurrentMerchantCertificate() {
        String path = wechatPlatformCertificateService.refreshCurrentMerchantCertificate();
        PayMerchantConfig merchant = paymentMerchantService.requireCurrentMerchant();
        return buildCertificateStatus(merchant, path);
    }

    public ReconcileDiffDetailResponse getReconcileDiffDetail(Long id) {
        PayReconcileRecord record = payReconcileRecordMapper.selectById(id);
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
}
