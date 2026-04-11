package com.qsd.admin.content.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record ServiceLineContentResponse(
    String pageCode,
    String lineCode,
    String lineName,
    String linePath,
    String status,
    String updatedAt,
    String publishedAt,
    JsonNode form
) {
}
