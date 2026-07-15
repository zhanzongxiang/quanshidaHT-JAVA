package com.qsd.admin.member.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.member.dto.MemberPackageResponse;
import com.qsd.admin.member.service.MemberPackageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/member/packages")
@PreAuthorize("hasAuthority('USER_TYPE_MEMBER')")
public class MemberPackageController {
    private final MemberPackageService memberPackageService;

    public MemberPackageController(MemberPackageService memberPackageService) {
        this.memberPackageService = memberPackageService;
    }

    @GetMapping("/inventory")
    public ApiResponse<List<MemberPackageResponse>> listInventory(Authentication authentication) {
        return ApiResponse.ok(memberPackageService.listInventory(authentication.getName()));
    }
}
