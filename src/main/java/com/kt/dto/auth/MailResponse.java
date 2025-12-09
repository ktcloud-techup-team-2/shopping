package com.kt.dto.auth;

public record MailResponse(
        boolean success,
        String message,
        int verificationCode
) {
}
