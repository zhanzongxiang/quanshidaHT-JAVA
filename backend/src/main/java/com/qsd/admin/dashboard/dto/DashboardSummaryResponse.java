package com.qsd.admin.dashboard.dto;

public record DashboardSummaryResponse(
    long waybillTotal,
    long waybillInTransit,
    long newsTotal,
    long newsPublished,
    int serviceLineTotal,
    String homeContentStatus,
    String homeContentUpdatedAt
) {
}
