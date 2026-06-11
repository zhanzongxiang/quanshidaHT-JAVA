package com.qsd.admin.payment.service;

import com.qsd.admin.common.exception.BusinessException;
import com.qsd.admin.common.exception.ErrorCode;
import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.common.service.CryptoService;
import com.qsd.admin.config.WechatPayProperties;
import com.qsd.admin.payment.dto.PayMerchantConfigCreateRequest;
import com.qsd.admin.payment.dto.PayMerchantConfigSummaryResponse;
import com.qsd.admin.payment.dto.PayMerchantConfigUpdateRequest;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.mapper.PayMerchantConfigMapper;
import com.qsd.admin.tenant.TenantContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentMerchantService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PayMerchantConfigMapper payMerchantConfigMapper;
    private final WechatPayProperties wechatPayProperties;
    private final CryptoService cryptoService;

    public PaymentMerchantService(
        PayMerchantConfigMapper payMerchantConfigMapper,
        WechatPayProperties wechatPayProperties,
        CryptoService cryptoService
    ) {
        this.payMerchantConfigMapper = payMerchantConfigMapper;
        this.wechatPayProperties = wechatPayProperties;
        this.cryptoService = cryptoService;
    }

    public List<PayMerchantConfigSummaryResponse> listMerchantConfigs() {
        Long tenantId = TenantContextHolder.requireTenantId();
        ensureDefaultMerchantExists(tenantId);
        return payMerchantConfigMapper.selectAllActiveRows(tenantId).stream()
            .map(this::toSummary)
            .toList();
    }

    public PayMerchantConfig requireCurrentMerchant() {
        Long tenantId = TenantContextHolder.requireTenantId();
        ensureDefaultMerchantExists(tenantId);
        PayMerchantConfig config = payMerchantConfigMapper.selectCurrentActive(tenantId);
        if (config != null) {
            return decryptMerchantConfig(config);
        }
        throw new IllegalStateException(PaymentMerchantExceptionMessages.CURRENT_MERCHANT_MISSING);
    }

    public PayMerchantConfig requireMerchantById(Long id) {
        Long tenantId = TenantContextHolder.requireTenantId();
        ensureDefaultMerchantExists(tenantId);
        PayMerchantConfig config = payMerchantConfigMapper.selectActiveById(tenantId, id);
        if (config == null) {
            throw new NotFoundException(PaymentMerchantExceptionMessages.MERCHANT_NOT_FOUND);
        }
        return decryptMerchantConfig(config);
    }

    private PayMerchantConfig decryptMerchantConfig(PayMerchantConfig config) {
        if (config == null) return null;
        config.setAppSecret(decryptSensitive(config.getAppSecret()));
        config.setApiV3Key(decryptSensitive(config.getApiV3Key()));
        return config;
    }

    public PayMerchantConfig findMerchantByMchId(String mchId) {
        String normalized = trimToNull(mchId);
        if (normalized == null) {
            return null;
        }
        Long tenantId = TenantContextHolder.requireTenantId();
        ensureDefaultMerchantExists(tenantId);
        return decryptMerchantConfig(payMerchantConfigMapper.selectByMchId(tenantId, normalized));
    }

    public List<PayMerchantConfig> listMerchantEntities() {
        Long tenantId = TenantContextHolder.requireTenantId();
        ensureDefaultMerchantExists(tenantId);
        return payMerchantConfigMapper.selectAllActiveRows(tenantId);
    }

    public PayMerchantConfig findMerchantByMchIdGlobal(String mchId) {
        String normalized = trimToNull(mchId);
        if (normalized == null) {
            return null;
        }
        return decryptMerchantConfig(payMerchantConfigMapper.selectByMchIdGlobal(normalized));
    }

    public List<PayMerchantConfig> listMerchantEntitiesGlobal() {
        return payMerchantConfigMapper.selectAllActiveRowsGlobal();
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
        Long tenantId = TenantContextHolder.requireTenantId();
        ensureDefaultMerchantExists(tenantId);
        validateMerchantRequest(
            request.notifyUrl(),
            request.apiV3Key(),
            request.privateKeyPath(),
            request.merchantSerialNo(),
            request.platformCertificatePath()
        );
        LocalDateTime now = LocalDateTime.now();
        PayMerchantConfig config = new PayMerchantConfig();
        config.setTenantId(tenantId);
        config.setMerchantName(request.merchantName().trim());
        config.setMerchantCode(request.merchantCode().trim());
        config.setMchId(request.mchId().trim());
        config.setAppId(request.appId().trim());
        config.setAppSecret(encryptSensitive(request.appSecret()));
        config.setNotifyUrl(request.notifyUrl().trim());
        config.setApiV3Key(encryptSensitive(request.apiV3Key()));
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
        validateMerchantRequest(
            request.notifyUrl(),
            request.apiV3Key(),
            request.privateKeyPath(),
            request.merchantSerialNo(),
            request.platformCertificatePath()
        );
        PayMerchantConfig config = requireMerchantById(id);
        config.setMerchantName(request.merchantName().trim());
        config.setMerchantCode(request.merchantCode().trim());
        config.setMchId(request.mchId().trim());
        config.setAppId(request.appId().trim());
        config.setAppSecret(encryptSensitive(request.appSecret()));
        config.setNotifyUrl(request.notifyUrl().trim());
        config.setApiV3Key(encryptSensitive(request.apiV3Key()));
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
        Long tenantId = TenantContextHolder.requireTenantId();
        PayMerchantConfig config = requireMerchantById(id);
        if (config.getEnabled() == null || config.getEnabled() != 1) {
            throw new BusinessException(PaymentMerchantExceptionMessages.MERCHANT_DISABLED);
        }
        payMerchantConfigMapper.clearActiveFlag(tenantId);
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

    private void ensureDefaultMerchantExists(Long tenantId) {
        if (!payMerchantConfigMapper.selectAllActiveRows(tenantId).isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        PayMerchantConfig config = buildFallbackMerchantFromProperties();
        config.setTenantId(tenantId);
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
        } else if (!isValidNotifyUrl(config.getNotifyUrl())) {
            issues.add("invalid_notify_url");
        }
        if (!hasValidApiV3Key(config.getApiV3Key())) {
            issues.add("invalid_api_v3_key_length");
        }
        if (hasAny(trimToNull(config.getPrivateKeyPath()), trimToNull(config.getMerchantSerialNo()), trimToNull(config.getPlatformCertificatePath()))
            && !hasAll(trimToNull(config.getPrivateKeyPath()), trimToNull(config.getMerchantSerialNo()), trimToNull(config.getPlatformCertificatePath()))) {
            issues.add("incomplete_certificate_bundle");
        }
        return issues;
    }

    public List<String> buildConfigurationIssuesForOps(PayMerchantConfig config) {
        return buildConfigurationIssues(config);
    }

    private void validateMerchantRequest(
        String notifyUrl,
        String apiV3Key,
        String privateKeyPath,
        String merchantSerialNo,
        String platformCertificatePath
    ) {
        if (!isValidNotifyUrl(notifyUrl)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "notifyUrl must be a valid http or https URL");
        }

        if (!hasValidApiV3Key(apiV3Key)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "apiV3Key must be exactly 32 characters when provided");
        }

        String normalizedPrivateKeyPath = trimToNull(privateKeyPath);
        String normalizedMerchantSerialNo = trimToNull(merchantSerialNo);
        String normalizedPlatformCertificatePath = trimToNull(platformCertificatePath);
        if (hasAny(normalizedPrivateKeyPath, normalizedMerchantSerialNo, normalizedPlatformCertificatePath)
            && !hasAll(normalizedPrivateKeyPath, normalizedMerchantSerialNo, normalizedPlatformCertificatePath)) {
            throw new BusinessException(
                ErrorCode.VALIDATION_FAILED,
                "privateKeyPath, merchantSerialNo, and platformCertificatePath must be provided together"
            );
        }
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

    private String encryptSensitive(String value) {
        String trimmed = trimToEmpty(value);
        if (trimmed.isEmpty() || cryptoService.isEncrypted(trimmed)) {
            return trimmed;
        }
        return cryptoService.encrypt(trimmed);
    }

    private boolean isValidNotifyUrl(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return false;
        }
        try {
            URI uri = new URI(normalized);
            String scheme = trimToNull(uri.getScheme());
            String host = trimToNull(uri.getHost());
            if (scheme == null || host == null) {
                return false;
            }
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                return false;
            }
            if ("http".equalsIgnoreCase(scheme) && !isLocalHost(host)) {
                return false;
            }
            return true;
        } catch (URISyntaxException ex) {
            return false;
        }
    }

    private boolean hasValidApiV3Key(String value) {
        String normalized = trimToNull(value);
        return normalized == null || normalized.length() == 32;
    }

    private boolean isLocalHost(String host) {
        return "localhost".equalsIgnoreCase(host)
            || "127.0.0.1".equals(host)
            || "::1".equals(host);
    }

    private boolean hasAny(String... values) {
        for (String value : values) {
            if (value != null) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAll(String... values) {
        for (String value : values) {
            if (value == null) {
                return false;
            }
        }
        return true;
    }

    public String decryptSensitive(String value) {
        if (value == null || value.isEmpty()) return value;
        return cryptoService.decrypt(value);
    }
}
