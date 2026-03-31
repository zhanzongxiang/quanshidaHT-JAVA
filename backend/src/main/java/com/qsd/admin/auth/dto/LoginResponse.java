package com.qsd.admin.auth.dto;

public record LoginResponse(
    String accessToken,
    String tokenType
) {
}
