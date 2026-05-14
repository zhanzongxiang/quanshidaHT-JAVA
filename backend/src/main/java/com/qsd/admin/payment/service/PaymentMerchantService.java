package com.qsd.admin.payment.service;

import com.qsd.admin.common.exception.BusinessException;
import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.config.WechatPayProperties;
import com.qsd.admin.payment.dto.PayMerchantConfigCreateRequest;
import com.qsd.admin.payment.dto.PayMerchantConfigSummaryResponse;
import com.qsd.admin.payment.dto.PayMerchantConfigUpdateRequest;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.mapper.PayMerchantConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentMerchantService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PayMerchantConfigMapper payMerchantConfigMapper;
    private final WechatPayProperties wechatPayProperties;

    public PaymentMerchantService(
        PayMerchantConfigMapper payMerchantConfigMapper,
        WechatPayProperties wechatPayProperties
    ) {
        this.payMerchantConfigMapper = payMerchantConfigMapper;
        this.wechatPayProperties = wechatPayProperties;
    }

    public List<PayMerchantConfigSummaryResponse> listMerchantConfigs() {
        ensureDefaultMerchantExists();
        return payMerchantConfigMapper.selectAllActiveRows().stream()
            .map(this::toSummary)
            .toList();
    }

    public PayMerchantConfig requireCurrentMerchant() {
        ensureDefaultMerchantExists();
        PayMerchantConfig config = payMerchantConfigMapper.selectCurrentActive();
        if (config != null) {
            return config;
        }
        throw new IllegalStateException(PaymentMerchantExceptionMessages.CURRENT_MERCHANT_MISSING);
    }

    public PayMerchantConfig requireMerchantById(Long id) {
        ensureDefaultMerchantExists();
        PayMerchantConfig config = payMerchantConfigMapper.selectActiveById(id);
        if (config == null) {
            throw new NotFoundException(PaymentMerchantExceptionMessages.MERCHANT_NOT_FOUND);
        }
        return config;
    }

    public PayMerchantConfig findMerchantByMchId(String mchId) {
        String normalized = trimToNull(mchId);
        if (normalized == null) {
            return null;
        }
        ensureDefaultMerchantExists();
        return payMerchantConfigMapper.selectByMchId(normalized);
    }

    public List<PayMerchantConfig> listMerchantEntities() {
        ensureDefaultMerchantExists();
        return payMerchantConfigMapper.selectAllActiveRows();
    }

    public PayMerchantConfig buildFallbackMerchantFromProperties() {
        PayMerchantConfig config = new PayMerchantConfig();
        config.setMerchantName(trimToDefault(wechatPayProperties.merchantName(), "Default Merchant"));
        config.setMerchantCode("property_default");
        config.setMchId(trimToDefault(wechatPayProperties.mchId(), "demo-mch"));
        config.setAppId(trimToDefault(wechatPayProperties.appId(), "wx-demo-miniapp"));
        config.setAppSecret(trimToEmpty(wechatPayProperties.appSecret()));
        config.setNotifyUrl(trimToDefault(wechatPayProperties.notifyUrl(), "http://localhost:8080/api/payment/callback/wechat"));
        config.setApiV3Key(trimToEmpty(wechatPayProperties.apiV3Key()));
        config.setPrivateKeyPath(trimToEmpty(wechatPayProperties.privateKeyPath()));
        config.setMerchantSerialNo(trimToEmpty(wechatPayProperties.merchantSerialNo()));
        config.setPlatformCertificatePath(trimToEmpty(wechatPayProperties.platformCertificatePath()));
        config.setEnabled(1);
        config.setActive(1);
        config.setDeleted(0);
        return config;
    }

    @Transactional
    public PayMerchantConfigSummaryResponse createMerchantConfig(PayMerchantConfigCreateRequest request) {
        ensureDefaultMerchantExists();
        LocalDateTime now = LocalDateTime.now();
        PayMerchantConfig config = new PayMerchantConfig();
        config.setMerchantName(request.merchantName().trim());
        config.setMerchantCode(request.merchantCode().trim());
        config.setMchId(request.mchId().trim());
        config.setAppId(request.appId().trim());
        config.setAppSecret(trimToEmpty(request.appSecret()));
        config.setNotifyUrl(request.notifyUrl().trim());
        config.setApiV3Key(trimToEmpty(request.apiV3Key()));
        config.setPrivateKeyPath(trimToEmpty(request.privateKeyPath()));
        config.setMerchantSerialNo(trimToEmpty(request.merchantSerialNo()));
        config.setPlatformCertificatePath(trimToEmpty(request.platformCertificatePath()));
        config.setEnabled(request.enabled() == null || request.enabled() ? 1 : 0);
        config.setActive(0);
        config.setRemark(trimToEmpty(request.remark()));
        config.setDeleted(0);
        config.setCreatedAt(now);
        config.setUpdatedAt(now);
        payMerchantConfigMapper.insert(config);
        return toSummary(config);
    }

    @Transactional
    public PayMerchantConfigSummaryResponse updateMerchantConfig(Long id, PayMerchantConfigUpdateRequest request) {
        PayMerchantConfig config = requireMerchantById(id);
        config.setMerchantName(request.merchantName().trim());
        config.setMerchantCode(request.merchantCode().trim());
        config.setMchId(request.mchId().trim());
        config.setAppId(request.appId().trim());
        config.setAppSecret(trimToEmpty(request.appSecret()));
        config.setNotifyUrl(request.notifyUrl().trim());
        config.setApiV3Key(trimToEmpty(request.apiV3Key()));
        config.setPrivateKeyPath(trimToEmpty(request.privateKeyPath()));
        config.setMerchantSerialNo(trimToEmpty(request.merchantSerialNo()));
        config.setPlatformCertificatePath(trimToEmpty(request.platformCertificatePath()));
        config.setEnabled(request.enabled() ? 1 : 0);
        config.setRemark(trimToEmpty(request.remark()));
        config.setUpdatedAt(LocalDateTime.now());
        payMerchantConfigMapper.updateById(config);
        return toSummary(requireMerchantById(id));
    }

    @Transactional
    public PayMerchantConfigSummaryResponse activateMerchantConfig(Long id) {
        PayMerchantConfig config = requireMerchantById(id);
        if (config.getEnabled() == null || config.getEnabled() != 1) {
            throw new BusinessException(PaymentMerchantExceptionMessages.MERCHANT_DISABLED);
        }
        payMerchantConfigMapper.clearActiveFlag();
        config.setActive(1);
        config.setUpdatedAt(LocalDateTime.now());
        payMerchantConfigMapper.updateById(config);
        return toSummary(requireMerchantById(id));
    }

    @Transactional
    public void updateMerchantPlatformCertificatePath(Long id, String platformCertificatePath) {
        PayMerchantConfig config = requireMerchantById(id);
        config.setPlatformCertificatePath(trimToEmpty(platformCertificatePath));
        config.setUpdatedAt(LocalDateTime.now());
        payMerchantConfigMapper.updateById(config);
    }

    private void ensureDefaultMerchantExists() {
        if (!payMerchantConfigMapper.selectAllActiveRows().isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        PayMerchantConfig config = buildFallbackMerchantFromProperties();
        config.setMerchantCode("default_merchant");
        config.setRemark("Bootstrapped from application.yml");
        config.setCreatedAt(now);
        config.setUpdatedAt(now);
        payMerchantConfigMapper.insert(config);
    }

    private PayMerchantConfigSummaryResponse toSummary(PayMerchantConfig config) {
        List<String> configurationIssues = buildConfigurationIssues(config);
        boolean configurationReady = configurationIssues.isEmpty();
        return new PayMerchantConfigSummaryResponse(
            config.getId(),
            config.getMerchantName(),
            config.getMerchantCode(),
            config.getMchId(),
            config.getAppId(),
            config.getAppSecret() != null && !config.getAppSecret().isBlank(),
            config.getNotifyUrl(),
            config.getApiV3Key() != null && !config.getApiV3Key().isBlank(),
            config.getPrivateKeyPath() != null && !config.getPrivateKeyPath().isBlank(),
            config.getMerchantSerialNo() != null && !config.getMerchantSerialNo().isBlank(),
            config.getPlatformCertificatePath() != null && !config.getPlatformCertificatePath().isBlank(),
            configurationReady,
            configurationReady ? "ready" : "incomplete",
            configurationIssues,
            config.getEnabled() != null && config.getEnabled() == 1,
            config.getActive() != null && config.getActive() == 1,
            trimToEmpty(config.getRemark()),
            formatDateTime(config.getCreatedAt()),
            formatDateTime(config.getUpdatedAt())
        );
    }

    private List<String> buildConfigurationIssues(PayMerchantConfig config) {
        List<String> issues = new ArrayList<>();
        if (config.getAppSecret() == null || config.getAppSecret().isBlank()) {
            issues.add("missing_app_secret");
        }
        if (config.getApiV3Key() == null || config.getApiV3Key().isBlank()) {
            issues.add("missing_api_v3_key");
        }
        if (config.getPrivateKeyPath() == null || config.getPrivateKeyPath().isBlank()) {
            issues.add("missing_private_key_path");
        }
        if (config.getMerchantSerialNo() == null || config.getMerchantSerialNo().isBlank()) {
            issues.add("missing_merchant_serial_no");
        }
        if (config.getPlatformCertificatePath() == null || config.getPlatformCertificatePath().isBlank()) {
            issues.add("missing_platform_certificate_path");
        }
        if (config.getNotifyUrl() == null || config.getNotifyUrl().isBlank()) {
            issues.add("missing_notify_url");
        }
        return issues;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : DATE_TIME_FORMATTER.format(value);
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToDefault(String value, String fallback) {
        String normalized = trimToNull(value);
        return normalized == null || normalized.isEmpty() ? fallback : normalized;
    }
}
