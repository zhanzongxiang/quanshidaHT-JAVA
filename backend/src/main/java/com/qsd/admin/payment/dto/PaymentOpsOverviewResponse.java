package com.qsd.admin.payment.dto;

import java.util.List;

public record PaymentOpsOverviewResponse(
    MerchantCertificateStatusResponse currentMerchantCertificate,
    List<NotifyFailureStatResponse> paymentNotifyFailures,
    List<NotifyFailureStatResponse> refundNotifyFailures
) {
}
