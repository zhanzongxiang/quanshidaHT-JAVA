package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberRegisterRequest(
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^1\\d{10}$", message = "Phone must be an 11-digit mobile number")
    String phone,

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 64, message = "Password must be 6 to 64 characters")
    String password,

    @Size(max = 64, message = "Nickname must be at most 64 characters")
    String nickname,

    @Size(max = 64, message = "Full name must be at most 64 characters")
    String fullName
) {
}
