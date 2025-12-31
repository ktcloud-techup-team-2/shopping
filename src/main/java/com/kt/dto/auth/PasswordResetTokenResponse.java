package com.kt.dto.auth;

public record PasswordResetTokenResponse(
        boolean success,
        String message,
        String resetToken
) {
    public static PasswordResetTokenResponse of(String resetToken) {
        return new PasswordResetTokenResponse(true, "인증이 완료되었습니다.", resetToken);
    }
}
