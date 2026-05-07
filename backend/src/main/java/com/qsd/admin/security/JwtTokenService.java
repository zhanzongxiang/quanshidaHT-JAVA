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

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret()));
    }

    public String createAdminToken(Long userId, String username, List<String> permissions) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("uid", userId);
        claims.put("tokenType", TOKEN_TYPE_ADMIN);
        claims.put("permissions", permissions);
        return createToken(username, claims);
    }

    public String createMemberToken(Long memberId, String phone) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("uid", memberId);
        claims.put("tokenType", TOKEN_TYPE_MEMBER);
        return createToken(phone, claims);
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
}
