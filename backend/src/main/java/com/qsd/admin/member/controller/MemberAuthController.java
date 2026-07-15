package com.qsd.admin.member.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.member.dto.MemberLoginRequest;
import com.qsd.admin.member.dto.MemberLoginResponse;
import com.qsd.admin.member.dto.MemberMeResponse;
import com.qsd.admin.member.service.MemberAuthService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member-auth")
public class MemberAuthController {
    private final MemberAuthService memberAuthService;

    public MemberAuthController(MemberAuthService memberAuthService) {
        this.memberAuthService = memberAuthService;
    }

    @PostMapping("/login")
    public ApiResponse<MemberLoginResponse> login(@Valid @RequestBody MemberLoginRequest request) {
        return ApiResponse.ok(memberAuthService.login(request.account(), request.password()));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('USER_TYPE_MEMBER')")
    public ApiResponse<MemberMeResponse> me(Authentication authentication) {
        return ApiResponse.ok(memberAuthService.me(authentication.getName()));
    }
}
