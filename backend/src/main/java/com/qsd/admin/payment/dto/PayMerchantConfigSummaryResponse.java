package com.qsd.admin.payment.dto;

import java.util.List;

public record PayMerchantConfigSummaryResponse(
    Long id,
    String merchantName,
    String merchantCode,
    String mchId,
    String appId,
    boolean appSecretConfigured,
    String notifyUrl,
    boolean apiV3KeyConfigured,
    boolean privateKeyConfigured,
    boolean merchantSerialNoConfigured,
    boolean platformCertificateConfigured,
    boolean configurationReady,
    String configurationStatus,
    List<String> configurationIssues,
    boolean enabled,
    boolean active,
    String remark,
    String createdAt,
    String updatedAt
) {
}
