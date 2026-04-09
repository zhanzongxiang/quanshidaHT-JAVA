package com.qsd.admin.content.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record HomeContentResponse(
    String pageCode,
    String status,
    String updatedAt,
    String publishedAt,
    JsonNode form
) {
}
