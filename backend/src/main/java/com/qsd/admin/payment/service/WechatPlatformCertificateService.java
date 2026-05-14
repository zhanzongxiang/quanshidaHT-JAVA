package com.qsd.admin.payment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qsd.admin.config.WechatPayProperties;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WechatPlatformCertificateService {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final WechatPayCryptoService wechatPayCryptoService;
    private final PaymentMerchantService paymentMerchantService;
    private final WechatPayProperties wechatPayProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    public WechatPlatformCertificateService(
        ObjectMapper objectMapper,
        WechatPayCryptoService wechatPayCryptoService,
        PaymentMerchantService paymentMerchantService,
        WechatPayProperties wechatPayProperties
    ) {
        this.objectMapper = objectMapper;
        this.wechatPayCryptoService = wechatPayCryptoService;
        this.paymentMerchantService = paymentMerchantService;
        this.wechatPayProperties = wechatPayProperties;
    }

    public String refreshCurrentMerchantCertificate() {
        PayMerchantConfig merchant = paymentMerchantService.requireCurrentMerchant();
        return refreshMerchantCertificate(merchant);
    }

    @SuppressWarnings("unchecked")
    public String refreshMerchantCertificate(PayMerchantConfig merchant) {
        validateMerchant(merchant);
        try {
            String canonicalUrl = "/v3/certificates";
            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            String timestamp = String.valueOf(Instant.now().getEpochSecond());
            String authorization = buildAuthorizationHeader("GET", canonicalUrl, timestamp, nonceStr, "", merchant);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.set("Authorization", authorization);
            headers.set("Wechatpay-Serial", merchant.getMerchantSerialNo());
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                "https://api.mch.weixin.qq.com" + canonicalUrl,
                HttpMethod.GET,
                entity,
                String.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("微信支付平台证书请求失败");
            }

            Map<String, Object> body = objectMapper.readValue(response.getBody(), MAP_TYPE);
            Object dataObject = body.get("data");
            if (!(dataObject instanceof List<?> items) || items.isEmpty()) {
                throw new IllegalStateException("微信支付平台证书响应为空");
            }

            Map<String, Object> latest = null;
            for (Object item : items) {
                if (item instanceof Map<?, ?> map) {
                    latest = (Map<String, Object>) map;
                }
            }
            if (latest == null) {
                throw new IllegalStateException("微信支付平台证书响应中没有可用证书");
            }

            Map<String, Object> encryptCertificate = (Map<String, Object>) latest.get("encrypt_certificate");
            if (encryptCertificate == null) {
                throw new IllegalStateException("微信支付平台证书响应缺少加密证书信息");
            }

            String serialNo = stringValue(latest.get("serial_no"));
            String associatedData = stringValue(encryptCertificate.get("associated_data"));
            String nonce = stringValue(encryptCertificate.get("nonce"));
            String ciphertext = stringValue(encryptCertificate.get("ciphertext"));
            String pem = wechatPayCryptoService.decryptCertificateCiphertext(associatedData, nonce, ciphertext, merchant.getApiV3Key());

            Path storeDir = Path.of(wechatPayProperties.certificateStoreDir()).toAbsolutePath().normalize();
            Files.createDirectories(storeDir);
            Path target = storeDir.resolve(merchant.getMerchantCode() + "-" + serialNo + ".pem");
            Files.writeString(target, pem, StandardCharsets.UTF_8);

            String previousPath = merchant.getPlatformCertificatePath();
            merchant.setPlatformCertificatePath(target.toString());
            merchant.setUpdatedAt(java.time.LocalDateTime.now());
            paymentMerchantService.updateMerchantPlatformCertificatePath(merchant.getId(), merchant.getPlatformCertificatePath());
            wechatPayCryptoService.evictPlatformCertificate(previousPath);
            return target.toString();
        } catch (Exception ex) {
            if (ex instanceof IllegalStateException stateException) {
                throw stateException;
            }
            throw new IllegalStateException("刷新微信支付平台证书失败", ex);
        }
    }

    private String buildAuthorizationHeader(
        String method,
        String canonicalUrl,
        String timestamp,
        String nonceStr,
        String body,
        PayMerchantConfig merchant
    ) {
        String message = method + "\n"
            + canonicalUrl + "\n"
            + timestamp + "\n"
            + nonceStr + "\n"
            + body + "\n";
        String signature = wechatPayCryptoService.sign(message, merchant);
        return "WECHATPAY2-SHA256-RSA2048 "
            + "mchid=\"" + merchant.getMchId() + "\","
            + "nonce_str=\"" + nonceStr + "\","
            + "timestamp=\"" + timestamp + "\","
            + "serial_no=\"" + merchant.getMerchantSerialNo() + "\","
            + "signature=\"" + signature + "\"";
    }

    private void validateMerchant(PayMerchantConfig merchant) {
        if (merchant == null
            || isBlank(merchant.getMchId())
            || isBlank(merchant.getMerchantSerialNo())
            || isBlank(merchant.getPrivateKeyPath())
            || isBlank(merchant.getApiV3Key())
            || isBlank(merchant.getMerchantCode())) {
            throw new IllegalStateException("商户证书刷新所需配置不完整");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
