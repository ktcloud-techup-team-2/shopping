package com.kt.dto.auth;

public record EmailResponse(
        boolean success,
        String message,
        String verificationCode
) {}
