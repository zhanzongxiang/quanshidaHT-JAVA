package com.qsd.admin.payment.dto;

public record MerchantCertificateStatusResponse(
    Long merchantConfigId,
    String merchantName,
    String mchId,
    String certificatePath,
    boolean autoRefreshEnabled,
    String lastUpdatedAt
) {
}
