package com.qsd.admin.tenant;

import com.qsd.admin.security.JwtAuthenticationFilter;
import com.qsd.admin.security.JwtTokenService;
import com.qsd.admin.tenant.entity.Tenant;
import com.qsd.admin.tenant.service.TenantService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantContextFilter extends OncePerRequestFilter {
    public static final String TENANT_HEADER = "X-Tenant-Code";

    private final TenantService tenantService;
    private final JwtTokenService jwtTokenService;

    public TenantContextFilter(TenantService tenantService, JwtTokenService jwtTokenService) {
        this.tenantService = tenantService;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        try {
            Tenant tenant = resolveTenant(request, response);
            if (tenant == null) {
                return;
            }

            TenantContextHolder.set(new TenantContext(tenant.getId(), tenant.getTenantCode(), tenant.getTenantName()));
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }

    private Tenant resolveTenant(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String tenantCodeHeader = trimToNull(request.getHeader(TENANT_HEADER));
        if (tenantCodeHeader != null) {
            Tenant tenant = tenantService.findActiveTenantByCode(tenantCodeHeader);
            if (tenant == null) {
                writeBadRequest(response, "specified tenant does not exist or is disabled");
                return null;
            }
            return tenant;
        }

        Claims claims = tryParseClaims(request);
        if (claims != null) {
            Object tenantIdValue = claims.get("tenantId");
            if (tenantIdValue instanceof Number number) {
                Tenant tenant = tenantService.findActiveTenantById(number.longValue());
                if (tenant == null) {
                    writeBadRequest(response, "token tenant does not exist or is disabled");
                    return null;
                }
                return tenant;
            }

            String tenantCode = trimToNull(claims.get("tenantCode", String.class));
            if (tenantCode != null) {
                Tenant tenant = tenantService.findActiveTenantByCode(tenantCode);
                if (tenant == null) {
                    writeBadRequest(response, "token tenant does not exist or is disabled");
                    return null;
                }
                return tenant;
            }
        }

        String host = trimToNull(request.getServerName());
        if (host != null) {
            Tenant tenant = tenantService.findActiveTenantByDomain(host);
            if (tenant != null) {
                return tenant;
            }
        }

        return tenantService.requireDefaultTenant();
    }

    private Claims tryParseClaims(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return null;
        }

        try {
            return jwtTokenService.parse(token);
        } catch (Exception ex) {
            return null;
        }
    }

    private String extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JwtAuthenticationFilter.COOKIE_NAME.equals(cookie.getName())) {
                    String value = trimToNull(cookie.getValue());
                    if (value != null) {
                        return value;
                    }
                }
            }
        }

        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return trimToNull(authorization.substring(7));
        }
        return null;
    }

    private void writeBadRequest(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":4004,\"message\":\"" + message + "\",\"data\":null}");
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
