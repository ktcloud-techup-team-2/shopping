package com.kt.security.dto;

public record TokenResponseDto(
        String accessToken,
        String grantType,
        Long userId
) {
    public static TokenResponseDto of(String accessToken, Long userId) {
        return new TokenResponseDto(accessToken, "Bearer", userId);
    }
}
