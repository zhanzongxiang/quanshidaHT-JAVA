package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberStatusUpdateRequest(
    @NotBlank(message = "status must not be blank")
    String status
) {
}
