package com.kt.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_USER_ID(HttpStatus.CONFLICT,"이미 사용 중인 아이디입니다."),
    INVALID_PASSWORD_CHECK(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
		DELIVERY_ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "배송지를 찾을 수 없습니다."),
		DELIVERY_ADDRESS_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "삭제된 배송지입니다."),
		DELIVERY_ADDRESS_MAX_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "배송지는 최대 10개까지 등록 가능합니다."),
		DELIVERY_ADDRESS_DELETED_CANNOT_UPDATE(HttpStatus.BAD_REQUEST, "삭제된 배송지는 수정할 수 없습니다."),
		DELIVERY_ADDRESS_DELETED_CANNOT_SET_DEFAULT(HttpStatus.BAD_REQUEST, "삭제된 배송지는 기본 배송지로 설정할 수 없습니다."),
		DEFAULT_DELIVERY_ADDRESS_NOT_SET(HttpStatus.NOT_FOUND, "기본 배송지가 설정되지 않았습니다.");

    private final HttpStatus status;
    private final String message;
}
