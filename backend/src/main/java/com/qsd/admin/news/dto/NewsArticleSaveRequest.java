package com.qsd.admin.news.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewsArticleSaveRequest(
    @NotBlank(message = "title must not be blank")
    @Size(max = 120, message = "title max length is 120")
    String title,

    @NotBlank(message = "summary must not be blank")
    @Size(max = 500, message = "summary max length is 500")
    String summary,

    @Size(max = 500, message = "coverImageUrl max length is 500")
    String coverImageUrl,

    @NotBlank(message = "content must not be blank")
    String content,

    @Size(max = 64, message = "author max length is 64")
    String author
) {
}
