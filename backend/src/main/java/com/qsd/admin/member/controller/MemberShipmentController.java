package com.qsd.admin.member.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.member.dto.MemberShipmentCreateRequest;
import com.qsd.admin.member.dto.MemberShipmentResponse;
import com.qsd.admin.member.service.MemberShipmentService;
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
@RequestMapping("/api/member/shipments")
@PreAuthorize("hasAuthority('USER_TYPE_MEMBER')")
public class MemberShipmentController {
    private final MemberShipmentService memberShipmentService;

    public MemberShipmentController(MemberShipmentService memberShipmentService) {
        this.memberShipmentService = memberShipmentService;
    }

    @GetMapping
    public ApiResponse<List<MemberShipmentResponse>> list(Authentication authentication) {
        return ApiResponse.ok(memberShipmentService.list(authentication.getName()));
    }

    @PostMapping
    public ApiResponse<MemberShipmentResponse> create(
        Authentication authentication,
        @Valid @RequestBody MemberShipmentCreateRequest request
    ) {
        return ApiResponse.ok(memberShipmentService.create(authentication.getName(), request));
    }
}
