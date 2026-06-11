package com.qsd.admin.dashboard.dto;

public record DashboardSummaryResponse(
    Long tenantId,
    String tenantCode,
    String tenantName,
    String tenantStatus,
    String timezone,
    String locale,
    long enabledDomainCount,
    long enabledMerchantCount,
    long memberTotal,
    long memberEnabled,
    long memberWechatBound,
    long waybillTotal,
    long waybillInTransit,
    long payOrderTotal,
    long payOrderPaying,
    long payOrderPaid,
    long refundOrderTotal,
    long refundProcessing,
    long newsTotal,
    long newsPublished,
    int serviceLineTotal,
    String homeContentStatus,
    String homeContentUpdatedAt
) {
}
