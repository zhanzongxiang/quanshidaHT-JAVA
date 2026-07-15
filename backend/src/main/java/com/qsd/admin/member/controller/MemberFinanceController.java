package com.qsd.admin.member.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.member.dto.MemberFinanceRecordResponse;
import com.qsd.admin.member.service.MemberOrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/member/finance-records")
@PreAuthorize("hasAuthority('USER_TYPE_MEMBER')")
public class MemberFinanceController {
    private final MemberOrderService memberOrderService;

    public MemberFinanceController(MemberOrderService memberOrderService) {
        this.memberOrderService = memberOrderService;
    }

    @GetMapping
    public ApiResponse<List<MemberFinanceRecordResponse>> list(Authentication authentication) {
        return ApiResponse.ok(memberOrderService.listFinanceRecords(authentication.getName()));
    }
}
