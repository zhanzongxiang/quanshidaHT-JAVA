package com.qsd.admin.member.controller;

import com.qsd.admin.auth.dto.LoginResponse;
import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.member.dto.MemberLoginRequest;
import com.qsd.admin.member.dto.MemberRegisterRequest;
import com.qsd.admin.member.dto.MemberWechatLoginRequest;
import com.qsd.admin.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member/auth")
public class MemberAuthController {
    private final MemberService memberService;

    public MemberAuthController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody MemberRegisterRequest request) {
        return ApiResponse.ok(memberService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody MemberLoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        return ApiResponse.ok(memberService.login(request, clientIp));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @PostMapping("/wechat-login")
    public ApiResponse<LoginResponse> wechatLogin(@Valid @RequestBody MemberWechatLoginRequest request) {
        return ApiResponse.ok(memberService.wechatLogin(request));
    }
}
