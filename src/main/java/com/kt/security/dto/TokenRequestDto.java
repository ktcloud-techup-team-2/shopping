package com.kt.security.dto;

public record TokenRequestDto (
        Long userId,
        String grantType,
        String accessToken,
        String refreshToken
) {}
