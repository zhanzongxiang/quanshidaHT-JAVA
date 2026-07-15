package com.qsd.admin.member.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.member.dto.MemberOrderResponse;
import com.qsd.admin.member.service.MemberOrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/member/orders")
@PreAuthorize("hasAuthority('USER_TYPE_MEMBER')")
public class MemberOrderController {
    private final MemberOrderService memberOrderService;

    public MemberOrderController(MemberOrderService memberOrderService) {
        this.memberOrderService = memberOrderService;
    }

    @GetMapping
    public ApiResponse<List<MemberOrderResponse>> list(Authentication authentication) {
        return ApiResponse.ok(memberOrderService.listOrders(authentication.getName()));
    }
}
