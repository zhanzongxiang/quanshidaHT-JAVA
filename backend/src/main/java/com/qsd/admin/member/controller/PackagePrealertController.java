package com.qsd.admin.member.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.member.dto.PackagePrealertResponse;
import com.qsd.admin.member.dto.PackagePrealertSaveRequest;
import com.qsd.admin.member.service.PackagePrealertService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/member/prealerts")
@PreAuthorize("hasAuthority('USER_TYPE_MEMBER')")
public class PackagePrealertController {
    private final PackagePrealertService packagePrealertService;

    public PackagePrealertController(PackagePrealertService packagePrealertService) {
        this.packagePrealertService = packagePrealertService;
    }

    @GetMapping
    public ApiResponse<List<PackagePrealertResponse>> list(Authentication authentication) {
        return ApiResponse.ok(packagePrealertService.list(authentication.getName()));
    }

    @PostMapping
    public ApiResponse<PackagePrealertResponse> create(Authentication authentication, @Valid @RequestBody PackagePrealertSaveRequest request) {
        return ApiResponse.ok(packagePrealertService.create(authentication.getName(), request));
    }
}
