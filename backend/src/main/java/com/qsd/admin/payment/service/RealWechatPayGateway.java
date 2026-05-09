package com.qsd.admin.payment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qsd.admin.payment.dto.WechatCodeSessionResponse;
import com.qsd.admin.payment.dto.WechatMiniProgramPayParams;
import com.qsd.admin.payment.dto.WechatReconcileDownloadResult;
import com.qsd.admin.payment.dto.WechatRefundResult;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.entity.PayOrder;
import com.qsd.admin.payment.entity.RefundOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "app.wechat-pay", name = "mock-enabled", havingValue = "false")
public class RealWechatPayGateway implements WechatPayGateway {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final WechatPayCryptoService wechatPayCryptoService;

    public RealWechatPayGateway(
        ObjectMapper objectMapper,
        WechatPayCryptoService wechatPayCryptoService
    ) {
        this.objectMapper = objectMapper;
        this.wechatPayCryptoService = wechatPayCryptoService;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public WechatCodeSessionResponse exchangeCode(String code, PayMerchantConfig merchantConfig) {
        validateMiniProgramIdentity(merchantConfig);
        String url = UriComponentsBuilder
            .fromHttpUrl("https://api.weixin.qq.com/sns/jscode2session")
            .queryParam("appid", merchantConfig.getAppId())
            .queryParam("secret", merchantConfig.getAppSecret())
            .queryParam("js_code", code.trim())
            .queryParam("grant_type", "authorization_code")
            .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null) {
            throw new IllegalStateException("wechat code2session returned empty response");
        }
        if (response.get("errcode") != null) {
            throw new IllegalArgumentException("wechat code2session failed: " + response.get("errmsg"));
        }
        return new WechatCodeSessionResponse(
            stringValue(response.get("openid")),
            stringValue(response.get("unionid")),
            stringValue(response.get("session_key"))
        );
    }

    @Override
    public WechatMiniProgramPayParams prepareMiniProgramPayment(PayOrder order, String openid, PayMerchantConfig merchantConfig) {
        validatePayMerchant(merchantConfig);
        try {
            String requestBody = buildPrepayRequestBody(order, openid, merchantConfig);
            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            String timestamp = String.valueOf(Instant.now().getEpochSecond());
            String authorization = buildAuthorizationHeader("POST", "/v3/pay/transactions/jsapi", timestamp, nonceStr, requestBody, merchantConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_JSON_VALUE));
            headers.set("Authorization", authorization);
            headers.set("Wechatpay-Serial", merchantConfig.getMerchantSerialNo());
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi",
                HttpMethod.POST,
                entity,
                String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("wechat jsapi prepay request failed");
            }

            Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), MAP_TYPE);
            String prepayId = stringValue(responseMap.get("prepay_id"));
            if (prepayId.isEmpty()) {
                throw new IllegalStateException("wechat jsapi prepay id is missing");
            }

            String payPackage = "prepay_id=" + prepayId;
            String paySignNonce = UUID.randomUUID().toString().replace("-", "");
            String paySignTimestamp = String.valueOf(Instant.now().getEpochSecond());
            String paySignMessage = merchantConfig.getAppId() + "\n"
                + paySignTimestamp + "\n"
                + paySignNonce + "\n"
                + payPackage + "\n";
            String paySign = wechatPayCryptoService.sign(paySignMessage, merchantConfig);

            return new WechatMiniProgramPayParams(
                merchantConfig.getAppId(),
                paySignTimestamp,
                paySignNonce,
                payPackage,
                "RSA",
                paySign,
                prepayId,
                prepayId
            );
        } catch (Exception ex) {
            if (ex instanceof IllegalStateException stateException) {
                throw stateException;
            }
            if (ex instanceof IllegalArgumentException argumentException) {
                throw argumentException;
            }
            throw new IllegalStateException("failed to prepare real wechat mini-program payment", ex);
        }
    }

    @Override
    public WechatRefundResult createRefund(PayOrder order, RefundOrder refundOrder, PayMerchantConfig merchantConfig) {
        validatePayMerchant(merchantConfig);
        try {
            String requestBody = buildRefundRequestBody(order, refundOrder, merchantConfig);
            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            String timestamp = String.valueOf(Instant.now().getEpochSecond());
            String canonicalUrl = "/v3/refund/domestic/refunds";
            String authorization = buildAuthorizationHeader("POST", canonicalUrl, timestamp, nonceStr, requestBody, merchantConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_JSON_VALUE));
            headers.set("Authorization", authorization);
            headers.set("Wechatpay-Serial", merchantConfig.getMerchantSerialNo());
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                "https://api.mch.weixin.qq.com" + canonicalUrl,
                HttpMethod.POST,
                entity,
                String.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("wechat refund request failed");
            }

            Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), MAP_TYPE);
            return new WechatRefundResult(
                stringValue(responseMap.get("refund_id")),
                stringValue(responseMap.get("status")),
                response.getBody()
            );
        } catch (Exception ex) {
            if (ex instanceof IllegalStateException stateException) {
                throw stateException;
            }
            if (ex instanceof IllegalArgumentException argumentException) {
                throw argumentException;
            }
            throw new IllegalStateException("failed to create real wechat refund", ex);
        }
    }

    @Override
    public WechatReconcileDownloadResult downloadTradeBill(LocalDate billDate, PayMerchantConfig merchantConfig) {
        validatePayMerchant(merchantConfig);
        try {
            String canonicalUrl = "/v3/bill/tradebill?bill_date=" + billDate + "&bill_type=ALL";
            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            String timestamp = String.valueOf(Instant.now().getEpochSecond());
            String authorization = buildAuthorizationHeader("GET", canonicalUrl, timestamp, nonceStr, "", merchantConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_JSON_VALUE));
            headers.set("Authorization", authorization);
            headers.set("Wechatpay-Serial", merchantConfig.getMerchantSerialNo());
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                "https://api.mch.weixin.qq.com" + canonicalUrl,
                HttpMethod.GET,
                entity,
                String.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("wechat trade bill request failed");
            }

            Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), MAP_TYPE);
            String downloadUrl = stringValue(responseMap.get("download_url"));
            if (downloadUrl.isEmpty()) {
                throw new IllegalStateException("wechat trade bill download url is missing");
            }

            ResponseEntity<String> downloadResponse = restTemplate.exchange(downloadUrl, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
            if (!downloadResponse.getStatusCode().is2xxSuccessful() || downloadResponse.getBody() == null) {
                throw new IllegalStateException("wechat trade bill download failed");
            }
            return new WechatReconcileDownloadResult(billDate.toString(), downloadResponse.getBody(), downloadUrl);
        } catch (Exception ex) {
            if (ex instanceof IllegalStateException stateException) {
                throw stateException;
            }
            if (ex instanceof IllegalArgumentException argumentException) {
                throw argumentException;
            }
            throw new IllegalStateException("failed to download wechat trade bill", ex);
        }
    }

    private String buildPrepayRequestBody(PayOrder order, String openid, PayMerchantConfig merchantConfig) throws JsonProcessingException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("appid", merchantConfig.getAppId());
        payload.put("mchid", merchantConfig.getMchId());
        payload.put("description", trimToDefault(order.getDescription(), "Waybill payment"));
        payload.put("out_trade_no", order.getOrderNo());
        payload.put("notify_url", trimToDefault(merchantConfig.getNotifyUrl(), "http://localhost:8080/api/payment/callback/wechat"));

        Map<String, Object> amount = new LinkedHashMap<>();
        amount.put("total", toFen(order.getAmountTotal()));
        amount.put("currency", trimToDefault(order.getCurrency(), "CNY"));
        payload.put("amount", amount);

        Map<String, Object> payer = new LinkedHashMap<>();
        payer.put("openid", openid);
        payload.put("payer", payer);

        return objectMapper.writeValueAsString(payload);
    }

    private String buildRefundRequestBody(PayOrder order, RefundOrder refundOrder, PayMerchantConfig merchantConfig) throws JsonProcessingException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("out_trade_no", order.getOrderNo());
        payload.put("out_refund_no", refundOrder.getRefundNo());
        payload.put("reason", trimToDefault(refundOrder.getReason(), "member refund"));
        payload.put("notify_url", resolveRefundNotifyUrl(merchantConfig.getNotifyUrl()));

        Map<String, Object> amount = new LinkedHashMap<>();
        amount.put("refund", toFen(refundOrder.getAmountRefund()));
        amount.put("total", toFen(order.getAmountPaid()));
        amount.put("currency", trimToDefault(order.getCurrency(), "CNY"));
        payload.put("amount", amount);

        return objectMapper.writeValueAsString(payload);
    }

    private String buildAuthorizationHeader(
        String method,
        String canonicalUrl,
        String timestamp,
        String nonceStr,
        String body,
        PayMerchantConfig merchantConfig
    ) {
        String message = method + "\n"
            + canonicalUrl + "\n"
            + timestamp + "\n"
            + nonceStr + "\n"
            + body + "\n";
        String signature = wechatPayCryptoService.sign(message, merchantConfig);
        return "WECHATPAY2-SHA256-RSA2048 "
            + "mchid=\"" + merchantConfig.getMchId() + "\","
            + "nonce_str=\"" + nonceStr + "\","
            + "timestamp=\"" + timestamp + "\","
            + "serial_no=\"" + merchantConfig.getMerchantSerialNo() + "\","
            + "signature=\"" + signature + "\"";
    }

    private int toFen(BigDecimal amount) {
        return amount.movePointRight(2).intValueExact();
    }

    private String resolveRefundNotifyUrl(String notifyUrl) {
        String normalized = trimToDefault(notifyUrl, "http://localhost:8080/api/payment/callback/wechat");
        if (normalized.endsWith("/wechat")) {
            return normalized + "-refund";
        }
        return normalized;
    }

    private void validateMiniProgramIdentity(PayMerchantConfig merchantConfig) {
        if (trimToNull(merchantConfig.getAppId()) == null || trimToNull(merchantConfig.getAppSecret()) == null) {
            throw new IllegalStateException("wechat mini-program app id or app secret is not configured");
        }
    }

    private void validatePayMerchant(PayMerchantConfig merchantConfig) {
        validateMiniProgramIdentity(merchantConfig);
        if (trimToNull(merchantConfig.getMchId()) == null
            || trimToNull(merchantConfig.getMerchantSerialNo()) == null
            || trimToNull(merchantConfig.getPrivateKeyPath()) == null
            || trimToNull(merchantConfig.getNotifyUrl()) == null) {
            throw new IllegalStateException("wechat pay merchant credentials are incomplete");
        }
    }

    private String trimToDefault(String value, String fallback) {
        String normalized = trimToNull(value);
        return normalized == null ? fallback : normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
