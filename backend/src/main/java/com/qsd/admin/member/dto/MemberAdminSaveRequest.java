package com.qsd.admin.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MemberAdminSaveRequest(
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    String phone,

    @Size(max = 64, message = "密码长度不能超过 64 个字符")
    String password,

    @Size(max = 64, message = "昵称长度不能超过 64 个字符")
    String nickname,

    @Size(max = 64, message = "姓名长度不能超过 64 个字符")
    String fullName,

    @Size(max = 500, message = "头像地址长度不能超过 500 个字符")
    String avatarUrl,

    @NotBlank(message = "会员状态不能为空")
    String status,

    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    String remark,

    List<Long> waybillIds
) {
}
