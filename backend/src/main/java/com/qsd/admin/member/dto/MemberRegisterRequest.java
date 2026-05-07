package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberRegisterRequest(
    @NotBlank(message = "phone must not be blank")
    @Pattern(regexp = "^1\\d{10}$", message = "phone must be 11 digits")
    String phone,

    @NotBlank(message = "password must not be blank")
    @Size(max = 64, message = "password max length is 64")
    String password,

    @Size(max = 64, message = "nickname max length is 64")
    String nickname,

    @Size(max = 64, message = "fullName max length is 64")
    String fullName
) {
}
