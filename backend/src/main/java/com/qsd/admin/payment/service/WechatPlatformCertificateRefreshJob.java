package com.qsd.admin.payment.service;

import com.qsd.admin.config.WechatPayProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WechatPlatformCertificateRefreshJob {
    private final WechatPayProperties wechatPayProperties;
    private final WechatPlatformCertificateService wechatPlatformCertificateService;

    public WechatPlatformCertificateRefreshJob(
        WechatPayProperties wechatPayProperties,
        WechatPlatformCertificateService wechatPlatformCertificateService
    ) {
        this.wechatPayProperties = wechatPayProperties;
        this.wechatPlatformCertificateService = wechatPlatformCertificateService;
    }

    @Scheduled(initialDelay = 30000, fixedDelayString = "#{${app.wechat-pay.certificate-refresh-hours:6} * 60 * 60 * 1000}")
    public void refreshCurrentMerchantCertificate() {
        if (!wechatPayProperties.autoRefreshPlatformCertificates() || wechatPayProperties.mockEnabled()) {
            return;
        }
        try {
            wechatPlatformCertificateService.refreshCurrentMerchantCertificate();
        } catch (Exception ignored) {
            // Keep the scheduler resilient; failures should not stop subsequent refresh attempts.
        }
    }
}
