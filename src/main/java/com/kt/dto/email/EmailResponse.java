package com.kt.dto.email;

public class EmailResponse {
    public record AuthenticationResponse(
            boolean success,
            String message,
            String verificationCode
    ) {}

    public record FindIdResponse(
            boolean success,
            String message
    ) {
        public static EmailResponse.FindIdResponse ok() {
            return new EmailResponse.FindIdResponse(true, "아이디 안내 메일이 전송되었습니다.");
        }
    }

}