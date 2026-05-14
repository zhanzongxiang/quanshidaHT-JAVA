package com.qsd.admin.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WechatPayCryptoService {
    private static final String SIGN_ALGORITHM = "SHA256withRSA";

    private final ObjectMapper objectMapper;
    private final Map<String, PrivateKey> privateKeyCache = new ConcurrentHashMap<>();
    private final Map<String, PublicKey> platformKeyCache = new ConcurrentHashMap<>();

    public WechatPayCryptoService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String sign(String message, PayMerchantConfig merchantConfig) {
        try {
            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            signature.initSign(loadPrivateKey(merchantConfig));
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception ex) {
            throw new IllegalStateException("微信支付请求签名失败", ex);
        }
    }

    public void verifyCallbackSignature(
        String body,
        String timestamp,
        String nonce,
        String signatureValue,
        PayMerchantConfig merchantConfig
    ) {
        try {
            String message = timestamp + "\n" + nonce + "\n" + body + "\n";
            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            signature.initVerify(loadPlatformPublicKey(merchantConfig));
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            boolean verified = signature.verify(Base64.getDecoder().decode(signatureValue));
            if (!verified) {
                throw new IllegalArgumentException("微信回调签名校验失败");
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("校验微信回调签名失败", ex);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> decryptNotifyResource(String body, String apiV3Key) {
        try {
            Map<String, Object> root = objectMapper.readValue(body, Map.class);
            Object resourceObject = root.get("resource");
            if (!(resourceObject instanceof Map<?, ?> resource)) {
                throw new IllegalArgumentException("微信回调缺少加密资源数据");
            }

            String nonce = stringValue(resource.get("nonce"));
            String associatedData = stringValue(resource.get("associated_data"));
            String ciphertext = stringValue(resource.get("ciphertext"));

            byte[] cipherBytes = Base64.getDecoder().decode(ciphertext);
            byte[] keyBytes = apiV3Key.getBytes(StandardCharsets.UTF_8);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            if (!associatedData.isEmpty()) {
                cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
            }
            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return objectMapper.readValue(plainBytes, Map.class);
        } catch (Exception ex) {
            throw new IllegalStateException("解密微信支付回调资源失败", ex);
        }
    }

    public String decryptCertificateCiphertext(String associatedData, String nonce, String ciphertext, String apiV3Key) {
        try {
            byte[] cipherBytes = Base64.getDecoder().decode(ciphertext);
            byte[] keyBytes = apiV3Key.getBytes(StandardCharsets.UTF_8);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            if (associatedData != null && !associatedData.isBlank()) {
                cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
            }
            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("解密微信平台证书失败", ex);
        }
    }

    public void evictPlatformCertificate(String certPath) {
        if (certPath == null || certPath.isBlank()) {
            return;
        }
        platformKeyCache.remove(certPath.trim());
    }

    private PrivateKey loadPrivateKey(PayMerchantConfig merchantConfig) {
        String keyPath = trimToNull(merchantConfig.getPrivateKeyPath());
        if (keyPath == null) {
            throw new IllegalStateException("商户私钥路径未配置");
        }
        return privateKeyCache.computeIfAbsent(keyPath, this::readPrivateKey);
    }

    private PublicKey loadPlatformPublicKey(PayMerchantConfig merchantConfig) {
        String certPath = trimToNull(merchantConfig.getPlatformCertificatePath());
        if (certPath == null) {
            throw new IllegalStateException("商户平台证书路径未配置");
        }
        return platformKeyCache.computeIfAbsent(certPath, this::readPlatformPublicKey);
    }

    private PrivateKey readPrivateKey(String keyPath) {
        try {
            String pem = Files.readString(Path.of(keyPath), StandardCharsets.UTF_8);
            String normalized = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(normalized);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception ex) {
            throw new IllegalStateException("加载商户私钥失败", ex);
        }
    }

    private PublicKey readPlatformPublicKey(String certPath) {
        try {
            try (var inputStream = Files.newInputStream(Path.of(certPath))) {
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                X509Certificate certificate = (X509Certificate) factory.generateCertificate(inputStream);
                return certificate.getPublicKey();
            }
        } catch (Exception ex) {
            throw new IllegalStateException("加载微信平台证书失败", ex);
        }
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
