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
import java.util.List;
import java.util.Map;

@Service
public class JwtTokenService {
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret()));
    }

    public String createToken(Long userId, String username, List<String> permissions) {
        Instant now = Instant.now();
        return Jwts.builder()
            .issuer(jwtProperties.issuer())
            .subject(username)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(jwtProperties.expireMinutes(), ChronoUnit.MINUTES)))
            .claims(Map.of("uid", userId, "permissions", permissions))
            .signWith(secretKey)
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }
}
