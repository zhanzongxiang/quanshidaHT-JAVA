package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MemberAdminSaveRequest(
    @NotBlank(message = "phone must not be blank")
    @Pattern(regexp = "^1\\d{10}$", message = "phone must be 11 digits")
    String phone,

    @Size(max = 64, message = "password max length is 64")
    String password,

    @Size(max = 64, message = "nickname max length is 64")
    String nickname,

    @Size(max = 64, message = "fullName max length is 64")
    String fullName,

    @Size(max = 500, message = "avatarUrl max length is 500")
    String avatarUrl,

    @NotBlank(message = "status must not be blank")
    String status,

    @Size(max = 500, message = "remark max length is 500")
    String remark,

    List<Long> waybillIds
) {
}
