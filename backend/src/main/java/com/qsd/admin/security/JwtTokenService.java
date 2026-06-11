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

@Service
public class JwtTokenService {
    public static final String TOKEN_TYPE_ADMIN = "admin";
    public static final String TOKEN_TYPE_MEMBER = "member";
    public static final String TOKEN_TYPE_MEMBER_WECHAT_BIND = "member_wechat_bind";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret()));
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
