package com.qsd.admin.member.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.member.dto.MemberAddressResponse;
import com.qsd.admin.member.dto.MemberAddressSaveRequest;
import com.qsd.admin.member.service.MemberAddressService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/member/addresses")
@PreAuthorize("hasAuthority('USER_TYPE_MEMBER')")
public class MemberAddressController {
    private final MemberAddressService memberAddressService;

    public MemberAddressController(MemberAddressService memberAddressService) {
        this.memberAddressService = memberAddressService;
    }

    @GetMapping
    public ApiResponse<List<MemberAddressResponse>> list(Authentication authentication) {
        return ApiResponse.ok(memberAddressService.list(authentication.getName()));
    }

    @PostMapping
    public ApiResponse<MemberAddressResponse> create(Authentication authentication, @Valid @RequestBody MemberAddressSaveRequest request) {
        return ApiResponse.ok(memberAddressService.create(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<MemberAddressResponse> update(Authentication authentication, @PathVariable Long id, @Valid @RequestBody MemberAddressSaveRequest request) {
        return ApiResponse.ok(memberAddressService.update(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(Authentication authentication, @PathVariable Long id) {
        memberAddressService.delete(authentication.getName(), id);
        return ApiResponse.ok();
    }
}
