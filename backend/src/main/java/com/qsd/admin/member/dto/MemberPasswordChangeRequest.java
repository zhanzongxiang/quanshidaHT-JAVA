package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberPasswordChangeRequest(
    @NotBlank(message = "Current password is required")
    @Size(min = 6, max = 64, message = "Current password must be 6 to 64 characters")
    String currentPassword,

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 64, message = "New password must be 6 to 64 characters")
    String newPassword
) {
}
