package com.qsd.admin.website.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.common.exception.BusinessException;
import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.common.service.RateLimiterService;
import com.qsd.admin.website.service.PublicWebsiteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PublicWebsiteController {
    private final PublicWebsiteService publicWebsiteService;
    private final RateLimiterService rateLimiterService;

    public PublicWebsiteController(PublicWebsiteService publicWebsiteService, RateLimiterService rateLimiterService) {
        this.publicWebsiteService = publicWebsiteService;
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/site")
    public ApiResponse<JsonNode> getSiteConfig() {
        return ApiResponse.ok(publicWebsiteService.getSiteConfig());
    }

    @GetMapping("/pages/home")
    public ApiResponse<JsonNode> getHomePage() {
        return ApiResponse.ok(publicWebsiteService.getHomePage());
    }

    @GetMapping("/pages/about")
    public ApiResponse<JsonNode> getAboutPage() {
        return ApiResponse.ok(publicWebsiteService.getAboutPage());
    }

    @GetMapping("/pages/contact")
    public ApiResponse<JsonNode> getContactPage() {
        return ApiResponse.ok(publicWebsiteService.getContactPage());
    }

    @GetMapping("/pages/service-line/{key}")
    public ApiResponse<JsonNode> getServiceLinePage(@PathVariable String key) {
        return ApiResponse.ok(publicWebsiteService.getServiceLinePage(key));
    }

    @GetMapping("/pages/news")
    public ApiResponse<JsonNode> getNewsListPage(
        @RequestParam(required = false) String year,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.ok(publicWebsiteService.getNewsListPage(year, page, pageSize));
    }

    @GetMapping("/pages/news/{id}")
    public ApiResponse<JsonNode> getNewsDetailPage(@PathVariable Long id) {
        return ApiResponse.ok(publicWebsiteService.getNewsDetailPage(id));
    }

    @GetMapping("/tracking/{trackingNo}")
    public ApiResponse<JsonNode> getTrackingResult(@PathVariable String trackingNo, jakarta.servlet.http.HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = httpRequest.getRemoteAddr();
        }
        String rateLimitKey = "tracking:" + clientIp;
        if (!rateLimiterService.isAllowed(rateLimitKey)) {
            long remaining = rateLimiterService.getRemainingLockoutSeconds(rateLimitKey);
            throw new BusinessException("查询过于频繁，请 " + remaining + " 秒后再试");
        }
        return ApiResponse.ok(publicWebsiteService.getTrackingResult(trackingNo));
    }
}
