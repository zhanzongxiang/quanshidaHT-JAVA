package com.qsd.admin.member.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.member.dto.MemberAdminDetailResponse;
import com.qsd.admin.member.dto.MemberAdminSaveRequest;
import com.qsd.admin.member.dto.MemberAdminSummaryResponse;
import com.qsd.admin.member.dto.MemberStatusUpdateRequest;
import com.qsd.admin.member.service.MemberService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/admin/members")
public class MemberAdminController {
    private final MemberService memberService;

    public MemberAdminController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ApiResponse<List<MemberAdminSummaryResponse>> list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(memberService.listAdminMembers(keyword, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<MemberAdminDetailResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(memberService.getAdminMemberDetail(id));
    }

    @PostMapping
    public ApiResponse<MemberAdminDetailResponse> create(@Valid @RequestBody MemberAdminSaveRequest request) {
        return ApiResponse.ok(memberService.createAdminMember(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<MemberAdminDetailResponse> update(@PathVariable Long id, @Valid @RequestBody MemberAdminSaveRequest request) {
        return ApiResponse.ok(memberService.updateAdminMember(id, request));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<MemberAdminDetailResponse> updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody MemberStatusUpdateRequest request
    ) {
        return ApiResponse.ok(memberService.updateAdminMemberStatus(id, request.status()));
    }
}
