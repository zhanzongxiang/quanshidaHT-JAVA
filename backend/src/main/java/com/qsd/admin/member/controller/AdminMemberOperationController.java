package com.qsd.admin.member.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.member.dto.AdminMemberOrderResponse;
import com.qsd.admin.member.dto.AdminMemberPackageResponse;
import com.qsd.admin.member.dto.AdminMemberPrealertResponse;
import com.qsd.admin.member.dto.AdminMemberShipmentResponse;
import com.qsd.admin.member.dto.AdminPackageInboundRequest;
import com.qsd.admin.member.dto.AdminShipmentQuoteRequest;
import com.qsd.admin.member.dto.AdminStatusUpdateRequest;
import com.qsd.admin.member.service.AdminMemberOperationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/member-operations")
public class AdminMemberOperationController {
    private final AdminMemberOperationService adminMemberOperationService;

    public AdminMemberOperationController(AdminMemberOperationService adminMemberOperationService) {
        this.adminMemberOperationService = adminMemberOperationService;
    }

    @GetMapping("/prealerts")
    @PreAuthorize("hasAuthority('USER_TYPE_ADMIN') and hasAuthority('member:view')")
    public ApiResponse<List<AdminMemberPrealertResponse>> listPrealerts(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(adminMemberOperationService.listPrealerts(keyword, status));
    }

    @PostMapping("/prealerts/{id}/inbound")
    @PreAuthorize("hasAuthority('USER_TYPE_ADMIN') and hasAuthority('member:operate')")
    public ApiResponse<AdminMemberPackageResponse> inboundPrealert(
        @PathVariable Long id,
        @Valid @RequestBody AdminPackageInboundRequest request
    ) {
        return ApiResponse.ok(adminMemberOperationService.inboundPrealert(id, request));
    }

    @GetMapping("/packages")
    @PreAuthorize("hasAuthority('USER_TYPE_ADMIN') and hasAuthority('member:view')")
    public ApiResponse<List<AdminMemberPackageResponse>> listPackages(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(adminMemberOperationService.listPackages(keyword, status));
    }

    @GetMapping("/shipments")
    @PreAuthorize("hasAuthority('USER_TYPE_ADMIN') and hasAuthority('member:view')")
    public ApiResponse<List<AdminMemberShipmentResponse>> listShipments(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(adminMemberOperationService.listShipments(keyword, status));
    }

    @PostMapping("/shipments/{id}/quote")
    @PreAuthorize("hasAuthority('USER_TYPE_ADMIN') and hasAuthority('member:operate')")
    public ApiResponse<AdminMemberShipmentResponse> quoteShipment(
        @PathVariable Long id,
        @Valid @RequestBody AdminShipmentQuoteRequest request
    ) {
        return ApiResponse.ok(adminMemberOperationService.quoteShipment(id, request));
    }

    @PutMapping("/shipments/{id}/status")
    @PreAuthorize("hasAuthority('USER_TYPE_ADMIN') and hasAuthority('member:operate')")
    public ApiResponse<AdminMemberShipmentResponse> updateShipmentStatus(
        @PathVariable Long id,
        @Valid @RequestBody AdminStatusUpdateRequest request
    ) {
        return ApiResponse.ok(adminMemberOperationService.updateShipmentStatus(id, request.status()));
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAuthority('USER_TYPE_ADMIN') and hasAuthority('member:view')")
    public ApiResponse<List<AdminMemberOrderResponse>> listOrders(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(adminMemberOperationService.listOrders(keyword, status));
    }

    @PostMapping("/orders/{id}/mark-paid")
    @PreAuthorize("hasAuthority('USER_TYPE_ADMIN') and hasAuthority('member:operate')")
    public ApiResponse<AdminMemberOrderResponse> markOrderPaid(@PathVariable Long id) {
        return ApiResponse.ok(adminMemberOperationService.markOrderPaid(id));
    }
}
