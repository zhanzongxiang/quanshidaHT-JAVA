package com.qsd.admin.content.dto;

public record ServiceLineSummaryResponse(
    String lineCode,
    String lineName,
    String linePath,
    String description,
    String status,
    String updatedAt,
    String publishedAt
) {
}
