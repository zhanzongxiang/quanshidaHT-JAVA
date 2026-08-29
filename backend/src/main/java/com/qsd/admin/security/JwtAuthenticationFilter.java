package com.qsd.admin.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String COOKIE_NAME = "qsd_token";

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractTokenFromCookie(request);
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims;
        try {
            claims = jwtTokenService.parse(token);
        } catch (Exception ex) {
            filterChain.doFilter(request, response);
            return;
        }

        Object uidValue = claims.get("uid");
        Long userId = uidValue instanceof Number number ? number.longValue() : null;
        String username = claims.getSubject();
        String tokenType = claims.get("tokenType", String.class);
        if (tokenType == null) {
            String userType = claims.get("userType", String.class);
            tokenType = JwtTokenService.USER_TYPE_MEMBER.equals(userType)
                ? JwtTokenService.TOKEN_TYPE_MEMBER
                : JwtTokenService.TOKEN_TYPE_ADMIN;
        }

        Long tenantId = toLong(claims.get("tenantId"));
        String tenantCode = claims.get("tenantCode", String.class);
        Long sourceTenantId = toLong(claims.get("sourceTenantId"));
        if (sourceTenantId == null) {
            sourceTenantId = tenantId;
        }
        String sourceTenantCode = claims.get("sourceTenantCode", String.class);
        if (sourceTenantCode == null || sourceTenantCode.isBlank()) {
            sourceTenantCode = tenantCode;
        }

        AuthenticatedUser principal = new AuthenticatedUser(
            userId,
            username,
            tokenType,
            tenantId,
            tenantCode,
            sourceTenantId,
            sourceTenantCode
        );
        Collection<SimpleGrantedAuthority> authorities = extractAuthorities(claims, tokenType);
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(principal, null, authorities);
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }

    private Collection<SimpleGrantedAuthority> extractAuthorities(Claims claims, String tokenType) {
        if (JwtTokenService.TOKEN_TYPE_MEMBER.equals(tokenType)) {
            return List.of(
                new SimpleGrantedAuthority("ROLE_MEMBER"),
                new SimpleGrantedAuthority("USER_TYPE_MEMBER")
            );
        }

        Object permissionObj = claims.get("permissions");
        if (!(permissionObj instanceof List<?> permissions)) {
            return List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("USER_TYPE_ADMIN")
            );
        }

        List<SimpleGrantedAuthority> authorities = permissions.stream()
            .map(Object::toString)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toCollection(ArrayList::new));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("USER_TYPE_ADMIN"));
        return authorities;
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                String value = cookie.getValue();
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }

    private Long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }
}
