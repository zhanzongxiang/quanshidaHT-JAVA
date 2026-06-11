package com.qsd.admin.auth.controller;

import com.qsd.admin.auth.dto.LoginRequest;
import com.qsd.admin.auth.dto.MeResponse;
import com.qsd.admin.auth.dto.SwitchTenantRequest;
import com.qsd.admin.auth.service.AuthService;
import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.common.exception.BusinessException;
import com.qsd.admin.common.exception.ErrorCode;
import com.qsd.admin.security.AuthenticatedUser;
import com.qsd.admin.security.JwtAuthenticationFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<Void> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String clientIp = getClientIp(httpRequest);
        String token = authService.login(request.username(), request.password(), clientIp);
        writeAuthCookie(httpRequest, httpResponse, token, 24 * 60 * 60);

        return ApiResponse.ok(null);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletResponse httpResponse) {
        Cookie cookie = new Cookie(JwtAuthenticationFilter.COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/api");
        cookie.setMaxAge(0);
        httpResponse.addCookie(cookie);
        return ApiResponse.ok(null);
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED, "Authentication required");
        }
        return ApiResponse.ok(authService.me(user));
    }

    @PostMapping("/switch-tenant")
    public ApiResponse<Void> switchTenant(
        @Valid @RequestBody SwitchTenantRequest request,
        Authentication authentication,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED, "Authentication required");
        }

        String token = authService.switchTenant(user, request.tenantId());
        writeAuthCookie(httpRequest, httpResponse, token, 24 * 60 * 60);
        return ApiResponse.ok(null);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeAuthCookie(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String token, int maxAgeSeconds) {
        Cookie cookie = new Cookie(JwtAuthenticationFilter.COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/api");
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setSecure(httpRequest.isSecure());
        cookie.setAttribute("SameSite", "Lax");
        httpResponse.addCookie(cookie);
    }
}
