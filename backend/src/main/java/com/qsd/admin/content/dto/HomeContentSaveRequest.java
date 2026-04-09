package com.qsd.admin.content.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public record HomeContentSaveRequest(
    @NotNull(message = "form must not be null") JsonNode form
) {
}
