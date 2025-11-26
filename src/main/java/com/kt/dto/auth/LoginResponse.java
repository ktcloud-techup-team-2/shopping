package com.kt.dto.auth;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String loginId
) {

    public static LoginResponse of(String accessToken, Long userId, String loginId) {
        return new LoginResponse(accessToken, "Bearer", userId, loginId);
    }
}
