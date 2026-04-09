package com.qsd.admin.news.dto;

public record NewsArticleResponse(
    Long id,
    String title,
    String summary,
    String coverImageUrl,
    String content,
    String author,
    String status,
    String publishedAt,
    String updatedAt
) {
}
