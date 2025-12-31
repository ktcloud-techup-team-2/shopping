package com.kt.dto.auth;

public record FindIdResponse(
        boolean success,
        String message
) {
    public static FindIdResponse ok() {
        return new FindIdResponse(true, "아이디 안내 메일이 전송되었습니다.");
    }
}
