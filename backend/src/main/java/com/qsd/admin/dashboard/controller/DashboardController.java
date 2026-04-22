package com.qsd.admin.dashboard.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.dashboard.dto.DashboardSummaryResponse;
import com.qsd.admin.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> getSummary() {
        return ApiResponse.ok(dashboardService.getSummary());
    }
}
