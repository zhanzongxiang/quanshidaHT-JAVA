package com.qsd.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.wechat-pay")
public record WechatPayProperties(
    boolean mockEnabled,
    boolean autoRefreshPlatformCertificates,
    String certificateStoreDir,
    int certificateRefreshHours,
    String merchantName,
    String appId,
    String appSecret,
    String mchId,
    String notifyUrl,
    String apiV3Key,
    String privateKeyPath,
    String merchantSerialNo,
    String platformCertificatePath
) {
}
