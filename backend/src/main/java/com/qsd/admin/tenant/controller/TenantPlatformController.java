package com.qsd.admin.tenant.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.tenant.dto.TenantResponse;
import com.qsd.admin.tenant.dto.TenantSaveRequest;
import com.qsd.admin.tenant.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/platform/tenants")
public class TenantPlatformController {
    private final TenantService tenantService;

    public TenantPlatformController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping("/current")
    public ApiResponse<TenantResponse> getCurrentTenant() {
        return ApiResponse.ok(tenantService.getCurrentTenant());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('tenant:view')")
    public ApiResponse<List<TenantResponse>> listTenants() {
        return ApiResponse.ok(tenantService.listTenants());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('tenant:edit')")
    public ApiResponse<TenantResponse> createTenant(@Valid @RequestBody TenantSaveRequest request) {
        return ApiResponse.ok(tenantService.createTenant(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('tenant:edit')")
    public ApiResponse<TenantResponse> updateTenant(@PathVariable Long id, @Valid @RequestBody TenantSaveRequest request) {
        return ApiResponse.ok(tenantService.updateTenant(id, request));
    }
}
