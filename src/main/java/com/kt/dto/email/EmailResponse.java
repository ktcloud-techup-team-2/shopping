package com.kt.dto.email;

public class EmailResponse {
    public record AuthenticationResponse(
            boolean success,
            String message,
            String verificationCode
    ) {}

}