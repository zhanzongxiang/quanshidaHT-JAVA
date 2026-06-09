package com.qsd.admin.payment.dto;

import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.tenant.TenantContext;

import java.util.Map;

public record WechatCallbackContext(
    PayMerchantConfig merchantConfig,
    Map<String, Object> resource,
    TenantContext tenantContext
) {
}
