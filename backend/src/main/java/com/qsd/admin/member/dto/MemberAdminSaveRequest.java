package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MemberAdminSaveRequest(
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^1\\d{10}$", message = "Phone must be an 11-digit mobile number")
    String phone,

    @Size(min = 6, max = 64, message = "Password must be 6 to 64 characters")
    String password,

    @Size(max = 64, message = "Nickname must be at most 64 characters")
    String nickname,

    @Size(max = 64, message = "Full name must be at most 64 characters")
    String fullName,

    @Size(max = 500, message = "Avatar URL must be at most 500 characters")
    String avatarUrl,

    @NotBlank(message = "Status is required")
    String status,

    @Size(max = 500, message = "Remark must be at most 500 characters")
    String remark,

    List<Long> waybillIds
) {
}
