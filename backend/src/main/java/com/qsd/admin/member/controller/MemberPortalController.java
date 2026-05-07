package com.qsd.admin.member.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.member.dto.MemberProfileResponse;
import com.qsd.admin.member.dto.MemberProfileUpdateRequest;
import com.qsd.admin.member.dto.MemberWaybillDetailResponse;
import com.qsd.admin.member.dto.MemberWaybillSummaryResponse;
import com.qsd.admin.member.service.MemberService;
import com.qsd.admin.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/member")
public class MemberPortalController {
    private final MemberService memberService;

    public MemberPortalController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/profile")
    public ApiResponse<MemberProfileResponse> profile(Authentication authentication) {
        return ApiResponse.ok(memberService.getMemberProfile(currentMember(authentication)));
    }

    @PutMapping("/profile")
    public ApiResponse<MemberProfileResponse> updateProfile(
        Authentication authentication,
        @Valid @RequestBody MemberProfileUpdateRequest request
    ) {
        return ApiResponse.ok(memberService.updateMemberProfile(currentMember(authentication), request));
    }

    @GetMapping("/waybills")
    public ApiResponse<List<MemberWaybillSummaryResponse>> waybills(Authentication authentication) {
        return ApiResponse.ok(memberService.listMemberWaybills(currentMember(authentication)));
    }

    @GetMapping("/waybills/{id}")
    public ApiResponse<MemberWaybillDetailResponse> waybillDetail(
        Authentication authentication,
        @PathVariable Long id
    ) {
        return ApiResponse.ok(memberService.getMemberWaybillDetail(currentMember(authentication), id));
    }

    private Long currentMember(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalArgumentException("member identity is missing");
        }
        return user.userId();
    }
}
