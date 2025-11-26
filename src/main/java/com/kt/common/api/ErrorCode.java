package com.kt.common.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
		// ---------------- COMMON -------------------
		COMMON_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "요청 값이 유효하지 않습니다."),
		COMMON_INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
		INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

		// ---------------- AUTH -------------------
		AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
		// ---------------- USER -------------------
    INVALID_USER_ID(HttpStatus.CONFLICT,"이미 사용 중인 아이디입니다."),
    INVALID_PASSWORD_CHECK(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),

		// ---------------- DELIVERY -------------------
		DELIVERY_ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "배송지를 찾을 수 없습니다."),
		DELIVERY_ADDRESS_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "삭제된 배송지입니다."),
		DELIVERY_ADDRESS_MAX_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "배송지는 최대 10개까지 등록 가능합니다."),
		DELIVERY_ADDRESS_DELETED_CANNOT_UPDATE(HttpStatus.BAD_REQUEST, "삭제된 배송지는 수정할 수 없습니다."),
		DELIVERY_ADDRESS_DELETED_CANNOT_SET_DEFAULT(HttpStatus.BAD_REQUEST, "삭제된 배송지는 기본 배송지로 설정할 수 없습니다."),
		DEFAULT_DELIVERY_ADDRESS_NOT_SET(HttpStatus.NOT_FOUND, "기본 배송지가 설정되지 않았습니다."),

		// ---------------- PRODUCT -------------------
		PRODUCT_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "상품명은 필수 입력 값입니다."),
		PRODUCT_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "상품명은 200자를 초과할 수 없습니다."),
		PRODUCT_PRICE_BELOW_MINIMUM(HttpStatus.BAD_REQUEST, "상품 가격은 0 이상이어야 합니다."),
		PRODUCT_STOCK_BELOW_MINIMUM(HttpStatus.BAD_REQUEST, "재고 수량은 0 이상이어야 합니다."),
		PRODUCT_STOCK_DECREASE_QUANTITY_INVALID(HttpStatus.BAD_REQUEST, "재고 감소 수량은 0보다 커야 합니다."),
		PRODUCT_STOCK_INCREASE_QUANTITY_INVALID(HttpStatus.BAD_REQUEST, "재고 증가 수량은 0보다 커야 합니다."),

		PRODUCT_STOCK_NOT_ENOUGH(HttpStatus.CONFLICT, "재고가 부족합니다."),
		PRODUCT_STATUS_CHANGE_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 상품 상태에서 해당 상태로 변경할 수 없습니다."),

		PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다.");


	private final HttpStatus status;
    private final String message;
}
