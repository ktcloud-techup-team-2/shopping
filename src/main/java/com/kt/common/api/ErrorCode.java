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
	UNAUTHORIZED_CLIENT(HttpStatus.UNAUTHORIZED, "인증 토큰이 존재하지 않습니다."),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
	JWT_DECODE_FAIL(HttpStatus.UNAUTHORIZED, "잘못된 형식의 토큰입니다."),
	JWT_SIGNATURE_FAIL(HttpStatus.UNAUTHORIZED,"토큰 서명이 유효하지 않습니다."),
	MISSING_AUTHORITY(HttpStatus.UNAUTHORIZED, "토큰에 권한 정보가 없습니다."),
	PERMISSION_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),
	INTERNAL_SERVER_ERROR_JWT_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "JWT 예외 응답 처리 중 오류가 발생했습니다."),
	// ---------------- USER -------------------
	INVALID_USER_ID(HttpStatus.CONFLICT,"이미 사용 중인 아이디입니다."),
	INVALID_PASSWORD_CHECK(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 유저를 찾을 수 없습니다."),

	// ---------------- DELIVERY -------------------
	DELIVERY_ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "배송지를 찾을 수 없습니다."),
	DELIVERY_ADDRESS_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "삭제된 배송지입니다."),
	DELIVERY_ADDRESS_MAX_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "배송지는 최대 10개까지 등록 가능합니다."),
	DELIVERY_ADDRESS_DELETED_CANNOT_UPDATE(HttpStatus.BAD_REQUEST, "삭제된 배송지는 수정할 수 없습니다."),
	DELIVERY_ADDRESS_DELETED_CANNOT_SET_DEFAULT(HttpStatus.BAD_REQUEST, "삭제된 배송지는 기본 배송지로 설정할 수 없습니다."),
	DEFAULT_DELIVERY_ADDRESS_NOT_SET(HttpStatus.NOT_FOUND, "기본 배송지가 설정되지 않았습니다."),
	DELIVERY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "중복 배송입니다."),
	DELIVERY_NOT_IN_PENDING(HttpStatus.CONFLICT, "주문접수 상태에서만 준비를 시작할 수 있습니다."),
	DELIVERY_NOT_IN_PREPARING(HttpStatus.CONFLICT, "상품준비중 상태에서만 출고준비완료로 변경할 수 있습니다."),
	DELIVERY_NOT_IN_READY(HttpStatus.CONFLICT, "출고준비완료 상태에서만 배송을 시작할 수 있습니다."),
	DELIVERY_NOT_IN_SHIPPING(HttpStatus.CONFLICT, "배송중 상태에서만 배송완료 처리할 수 있습니다."),
	DELIVERY_CANCEL_NOT_ALLOWED(HttpStatus.CONFLICT, "배송중이거나 완료된 주문은 취소할 수 없습니다."),
	DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 방송 정보를 찾을 수 없습니다."),

	// ---------------- PRODUCT -------------------
	PRODUCT_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "상품명은 필수 입력 값입니다."),
	PRODUCT_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "상품명은 200자를 초과할 수 없습니다."),
	PRODUCT_PRICE_BELOW_MINIMUM(HttpStatus.BAD_REQUEST, "상품 가격은 0 이상이어야 합니다."),
	PRODUCT_STOCK_DECREASE_QUANTITY_INVALID(HttpStatus.BAD_REQUEST, "재고 감소 수량은 0보다 커야 합니다."),
	PRODUCT_STOCK_INCREASE_QUANTITY_INVALID(HttpStatus.BAD_REQUEST, "재고 증가 수량은 0보다 커야 합니다."),

	PRODUCT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 상품입니다."),
	PRODUCT_HARD_DELETE_NOT_ALLOWED(HttpStatus.CONFLICT, "임시 저장 상태가 아닌 상품은 완전 삭제할 수 없습니다."),

	PRODUCT_STOCK_NOT_ENOUGH(HttpStatus.CONFLICT, "재고가 부족합니다."),
	PRODUCT_STATUS_CHANGE_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 상품 상태에서 해당 상태로 변경할 수 없습니다."),

	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),

	// ---------------- ORDER_PRODUCT -------------------
	ORDER_PRODUCT_QUANTITY_MINIMUM(HttpStatus.BAD_REQUEST, "주문 상품 수량은 1 이상이어야 합니다.");

	private final HttpStatus status;
	private final String message;
}
