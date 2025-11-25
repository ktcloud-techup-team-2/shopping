package com.kt.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_USER_ID(HttpStatus.CONFLICT,"이미 사용 중인 아이디입니다."),
    INVALID_PASSWORD_CHECK(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
