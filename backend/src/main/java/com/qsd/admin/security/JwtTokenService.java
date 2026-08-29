package com.qsd.admin.security;

import com.qsd.admin.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;

@Service
public class JwtTokenService {
    public static final String TOKEN_TYPE_ADMIN = "admin";
    public static final String TOKEN_TYPE_MEMBER = "member";
    public static final String TOKEN_TYPE_MEMBER_WECHAT_BIND = "member_wechat_bind";

    // Kept for the legacy member-auth endpoint during the migration to typed tokens.
    public static final String USER_TYPE_ADMIN = "ADMIN";
    public static final String USER_TYPE_MEMBER = "MEMBER";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = buildSecretKey(jwtProperties.secret());
    }

    private SecretKey buildSecretKey(String configuredSecret) {
        String secret = configuredSecret == null ? "" : configuredSecret.trim();
        if (secret.isBlank() || secret.startsWith("${")) {
            throw new IllegalStateException(
                "JWT_SECRET is missing or unresolved. Configure a Base64/Base64URL secret with at least 32 decoded bytes."
            );
        }

        byte[] keyBytes = decodeConfiguredSecret(secret);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "JWT_SECRET is too short. Configure a secret with at least 32 decoded bytes (256 bits)."
            );
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] decodeConfiguredSecret(String secret) {
        try {
            return Decoders.BASE64.decode(secret);
        } catch (RuntimeException ignored) {
            try {
                return Decoders.BASE64URL.decode(secret);
            } catch (RuntimeException ignoredUrl) {
                byte[] rawBytes = secret.getBytes(StandardCharsets.UTF_8);
                if (rawBytes.length >= 32) {
                    return rawBytes;
                }
                throw new IllegalStateException(
                    "JWT_SECRET must be standard Base64, Base64URL, or a raw secret of at least 32 characters.",
                    ignoredUrl
                );
            }
        }
    }

    public String createAdminToken(
        Long userId,
        String username,
        Long sourceTenantId,
        String sourceTenantCode,
        Long tenantId,
        String tenantCode,
        List<String> permissions
    ) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("uid", userId);
        claims.put("tokenType", TOKEN_TYPE_ADMIN);
        if (sourceTenantId != null) {
            claims.put("sourceTenantId", sourceTenantId);
        }
        if (sourceTenantCode != null && !sourceTenantCode.isBlank()) {
            claims.put("sourceTenantCode", sourceTenantCode);
        }
        if (tenantId != null) {
            claims.put("tenantId", tenantId);
        }
        if (tenantCode != null && !tenantCode.isBlank()) {
            claims.put("tenantCode", tenantCode);
        }
        claims.put("permissions", permissions);
        return createToken(username, claims);
    }

    public String createMemberToken(Long memberId, String phone, Long tenantId, String tenantCode) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("uid", memberId);
        claims.put("tokenType", TOKEN_TYPE_MEMBER);
        if (tenantId != null) {
            claims.put("tenantId", tenantId);
        }
        if (tenantCode != null && !tenantCode.isBlank()) {
            claims.put("tenantCode", tenantCode);
        }
        return createToken(phone, claims);
    }

    public String createMemberWechatBindTicket(Long tenantId, String tenantCode, String openid, String unionid) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("tokenType", TOKEN_TYPE_MEMBER_WECHAT_BIND);
        if (tenantId != null) {
            claims.put("tenantId", tenantId);
        }
        if (tenantCode != null && !tenantCode.isBlank()) {
            claims.put("tenantCode", tenantCode);
        }
        claims.put("openid", openid);
        if (unionid != null && !unionid.isBlank()) {
            claims.put("unionid", unionid);
        }
        return createToken(openid, claims);
    }

    /** Compatibility for the older standalone member login service. */
    public String createToken(Long userId, String username, List<String> permissions, String userType) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("uid", userId);
        claims.put("permissions", permissions);
        claims.put("userType", userType);
        claims.put("tokenType", USER_TYPE_MEMBER.equals(userType) ? TOKEN_TYPE_MEMBER : TOKEN_TYPE_ADMIN);
        return createToken(username, claims);
    }

    private String createToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
            .issuer(jwtProperties.issuer())
            .subject(subject)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(jwtProperties.expireMinutes(), ChronoUnit.MINUTES)))
            .claims(claims)
            .signWith(secretKey)
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    public MemberWechatBindTicket parseMemberWechatBindTicket(String token) {
        Claims claims = parse(token);
        if (!TOKEN_TYPE_MEMBER_WECHAT_BIND.equals(claims.get("tokenType", String.class))) {
            throw new IllegalArgumentException("Invalid WeChat bind ticket");
        }
        return new MemberWechatBindTicket(
            claims.get("tenantId", Long.class),
            claims.get("tenantCode", String.class),
            claims.get("openid", String.class),
            claims.get("unionid", String.class)
        );
    }

    public record MemberWechatBindTicket(
        Long tenantId,
        String tenantCode,
        String openid,
        String unionid
    ) {
    }
}
