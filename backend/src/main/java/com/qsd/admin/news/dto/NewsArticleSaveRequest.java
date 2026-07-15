package com.qsd.admin.news.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NewsArticleSaveRequest(
    @NotBlank(message = "title must not be blank")
    @Size(max = 120, message = "title max length is 120")
    String title,

    @NotBlank(message = "summary must not be blank")
    @Size(max = 500, message = "summary max length is 500")
    String summary,

    @Size(max = 500, message = "封面图 URL 长度不能超过 500 个字符")
    @Pattern(regexp = "^(https?://.*|)$", message = "封面图 URL 必须以 http:// 或 https:// 开头")
    String coverImageUrl,

    @NotBlank(message = "文章内容不能为空")
    @Size(max = 100000, message = "文章内容长度不能超过 100000 个字符")
    String content,

    @Size(max = 64, message = "author max length is 64")
    String author
) {
}
