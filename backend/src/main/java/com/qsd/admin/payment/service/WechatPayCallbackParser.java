package com.qsd.admin.payment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qsd.admin.payment.dto.RefundCallbackRequest;
import com.qsd.admin.payment.dto.WechatCallbackContext;
import com.qsd.admin.payment.dto.WechatPayCallbackRequest;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WechatPayCallbackParser {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final PaymentMerchantService paymentMerchantService;
    private final WechatPayCryptoService wechatPayCryptoService;

    public WechatPayCallbackParser(
        ObjectMapper objectMapper,
        PaymentMerchantService paymentMerchantService,
        WechatPayCryptoService wechatPayCryptoService
    ) {
        this.objectMapper = objectMapper;
        this.paymentMerchantService = paymentMerchantService;
        this.wechatPayCryptoService = wechatPayCryptoService;
    }

    public WechatPayCallbackRequest parsePaymentCallback(String body) {
        try {
            Map<String, Object> payload = objectMapper.readValue(body, MAP_TYPE);
            if (payload.containsKey("orderNo")) {
                return objectMapper.convertValue(payload, WechatPayCallbackRequest.class);
            }

            Map<String, Object> resource = decryptNotifyResource(payload, body);
            return new WechatPayCallbackRequest(
                stringValue(resource.get("out_trade_no")),
                stringValue(resource.get("transaction_id")),
                stringValue(resource.get("trade_state")),
                body
            );
        } catch (WechatCallbackException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new WechatCallbackException("parse_error", "failed to parse wechat payment callback", false, ex);
        }
    }

    public RefundCallbackRequest parseRefundCallback(String body) {
        try {
            Map<String, Object> payload = objectMapper.readValue(body, MAP_TYPE);
            if (payload.containsKey("refundNo")) {
                return objectMapper.convertValue(payload, RefundCallbackRequest.class);
            }

            Map<String, Object> resource = decryptNotifyResource(payload, body);
            return new RefundCallbackRequest(
                stringValue(resource.get("out_refund_no")),
                stringValue(resource.get("refund_status")),
                stringValue(resource.get("refund_id")),
                body
            );
        } catch (WechatCallbackException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new WechatCallbackException("parse_error", "failed to parse wechat refund callback", false, ex);
        }
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public WechatCallbackContext parsePaymentCallbackContext(
        String body,
        String timestamp,
        String nonce,
        String signature
    ) {
        try {
            Map<String, Object> payload = objectMapper.readValue(body, MAP_TYPE);
            PayMerchantConfig merchantConfig = resolveMerchantForNotify(payload, body, timestamp, nonce, signature);
            Map<String, Object> resource = wechatPayCryptoService.decryptNotifyResource(body, merchantConfig.getApiV3Key());
            return new WechatCallbackContext(merchantConfig, resource);
        } catch (Exception ex) {
            throw new IllegalArgumentException("failed to parse wechat payment callback context", ex);
        }
    }

    public WechatCallbackContext parseRefundCallbackContext(
        String body,
        String timestamp,
        String nonce,
        String signature
    ) {
        try {
            Map<String, Object> payload = objectMapper.readValue(body, MAP_TYPE);
            PayMerchantConfig merchantConfig = resolveMerchantForNotify(payload, body, timestamp, nonce, signature);
            Map<String, Object> resource = wechatPayCryptoService.decryptNotifyResource(body, merchantConfig.getApiV3Key());
            return new WechatCallbackContext(merchantConfig, resource);
        } catch (Exception ex) {
            throw new IllegalArgumentException("failed to parse wechat refund callback context", ex);
        }
    }

    private Map<String, Object> decryptNotifyResource(Map<String, Object> payload, String body) {
        PayMerchantConfig config = resolveMerchantForNotify(payload, body, null, null, null);
        return wechatPayCryptoService.decryptNotifyResource(body, config.getApiV3Key());
    }

    private PayMerchantConfig resolveMerchantForNotify(
        Map<String, Object> payload,
        String body,
        String timestamp,
        String nonce,
        String signature
    ) {
        String mchId = extractMchId(payload);
        if (!mchId.isEmpty()) {
            PayMerchantConfig byMchId = paymentMerchantService.findMerchantByMchId(mchId);
            if (isUsableForNotify(byMchId)) {
                verifySignatureIfPresent(body, timestamp, nonce, signature, byMchId);
                return byMchId;
            }
        }

        for (PayMerchantConfig config : paymentMerchantService.listMerchantEntities()) {
            if (isUsableForNotify(config)) {
                try {
                    verifySignatureIfPresent(body, timestamp, nonce, signature, config);
                    return config;
                } catch (IllegalStateException | IllegalArgumentException ignored) {
                    // Try next merchant until one signature can be verified or one key can decrypt.
                }
            }
        }
        PayMerchantConfig fallback = paymentMerchantService.buildFallbackMerchantFromProperties();
        if (isUsableForNotify(fallback)) {
            verifySignatureIfPresent(body, timestamp, nonce, signature, fallback);
            return fallback;
        }
        throw new WechatCallbackException("merchant_config_missing", "no merchant configuration can verify or decrypt wechat notify resource", true);
    }

    private String extractMchId(Map<String, Object> payload) {
        return stringValue(payload.get("mchid"));
    }

    private boolean isUsableForNotify(PayMerchantConfig config) {
        return config != null
            && config.getApiV3Key() != null
            && !config.getApiV3Key().isBlank();
    }

    private void verifySignatureIfPresent(
        String body,
        String timestamp,
        String nonce,
        String signature,
        PayMerchantConfig merchantConfig
    ) {
        if (timestamp == null || nonce == null || signature == null
            || timestamp.isBlank() || nonce.isBlank() || signature.isBlank()) {
            return;
        }
        try {
            wechatPayCryptoService.verifyCallbackSignature(body, timestamp, nonce, signature, merchantConfig);
        } catch (IllegalArgumentException ex) {
            throw new WechatCallbackException("signature_invalid", ex.getMessage(), true, ex);
        } catch (IllegalStateException ex) {
            throw new WechatCallbackException("signature_verify_error", ex.getMessage(), true, ex);
        }
    }
}
