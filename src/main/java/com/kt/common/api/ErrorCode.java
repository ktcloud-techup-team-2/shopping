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
    PASSWORD_RESET_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "비밀번호 리셋 토큰이 유효하지 않습니다."),
    KAKAO_TOKEN_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "카카오 토큰 발급 요청에 실패했습니다."),
    KAKAO_TOKEN_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "카카오 토큰 응답이 올바르지 않습니다."),
    KAKAO_TOKEN_REQUEST_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "카카오 서버 응답 시간이 초과되었습니다."),
    KAKAO_USERINFO_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "카카오 사용자 정보 조회에 실패했습니다."),
    KAKAO_USERINFO_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "카카오 사용자 정보 응답이 올바르지 않습니다."),
    KAKAO_USERINFO_REQUEST_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "카카오 사용자 정보 조회 시간이 초과되었습니다."),
    KAKAO_UNLINK_FAILED(HttpStatus.BAD_GATEWAY, "카카오 연결 끊기에 실패했습니다."),

	// ---------------- USER -------------------
	INVALID_USER_ID(HttpStatus.CONFLICT,"이미 사용 중인 아이디입니다."),
	INVALID_PASSWORD_CHECK(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
	INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "기존 비밀번호와 일치하지 않습니다."),
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
	COURIER_NOT_FOUND(HttpStatus.NOT_FOUND, "택배사를 찾을 수 없습니다."),
	COURIER_CODE_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 택배사 코드입니다."),

	// ---------------- REVIEW ----------------
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),

	// ---------------- CATEGORY -------------------
	CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
	CATEGORY_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "카테고리 명은 필수입니다."),
	CATEGORY_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "카테고리 명은 100자를 초과할 수 없습니다."),
	CATEGORY_PET_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "카테고리 반려동물 분류는 필수입니다."),
	CATEGORY_STATUS_REQUIRED(HttpStatus.BAD_REQUEST, "카테고리 상태는 필수입니다."),
	CATEGORY_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 카테고리입니다."),
	CATEGORY_HAS_PRODUCTS(HttpStatus.CONFLICT, "하위에 연결된 상품이 있어 삭제할 수 없습니다."),

	// ---------------- PRODUCT -------------------
	PRODUCT_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "상품명은 필수 입력 값입니다."),
	PRODUCT_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "상품명은 200자를 초과할 수 없습니다."),
	PRODUCT_PRICE_BELOW_MINIMUM(HttpStatus.BAD_REQUEST, "상품 가격은 0 이상이어야 합니다."),
	PRODUCT_PET_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "상품 반려동물 분류는 필수입니다."),
	PRODUCT_STOCK_REQUIRED_FOR_ACTIVATION(HttpStatus.BAD_REQUEST, "바로 활성화하려면 재고가 1개 이상이어야 합니다."),

	PRODUCT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 상품입니다."),
	PRODUCT_HARD_DELETE_NOT_ALLOWED(HttpStatus.CONFLICT, "임시 저장 상태가 아닌 상품은 완전 삭제할 수 없습니다."),

	PRODUCT_STOCK_NOT_ENOUGH(HttpStatus.CONFLICT, "재고가 부족합니다."),
	PRODUCT_STATUS_CHANGE_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 상품 상태에서 해당 상태로 변경할 수 없습니다."),

	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),

	// ---------------- INVENTORY -------------------
	INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "재고 정보를 찾을 수 없습니다."),
	INVENTORY_EVENT_QUANTITY_INVALID(HttpStatus.BAD_REQUEST, "재고 이벤트 수량은 0보다 커야 합니다."),
	INVENTORY_NEGATIVE_AVAILABLE(HttpStatus.CONFLICT, "가용 재고가 0 미만이 될 수 없습니다."),
	INVENTORY_RESERVATION_NOT_FOUND(HttpStatus.CONFLICT, "예약된 재고가 부족합니다."),
	INVENTORY_OUTBOUND_NOT_RESERVED(HttpStatus.CONFLICT, "출고 처리할 예약 재고가 없습니다."),

	// ---------------- CART -------------------
	CART_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품이 이미 삭제되어 있습니다"),
	CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니를 찾을 수 없습니다."),
	CART_EMPTY(HttpStatus.BAD_REQUEST, "장바구니가 비어 있습니다."),

	// ---------------- ORDER -------------------
	DUPLICATE_ORDER_NUMBER(HttpStatus.CONFLICT, "이미 존재하는 주문입니다."),
	ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
	ORDER_NOT_MODIFIABLE(HttpStatus.CONFLICT, "현재 상태에서는 주문을 수정할 수 없습니다."),
	ORDER_CANCEL_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 상태에서는 주문을 취소할 수 없습니다."),
	ORDER_ALREADY_CANCELLED(HttpStatus.CONFLICT, "이미 취소된 주문입니다."),
	ORDER_NOT_PENDING(HttpStatus.CONFLICT, "결제 대기 상태의 주문만 결제 할 수 있습니다."),
	ORDER_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액과 주문 금액이 일치하지 않습니다."),
	ORDER_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 완료된 주문입니다."),
	PAYMENT_NOT_COMPLETED(HttpStatus.CONFLICT, "결제가 완료되지 않았습니다."),
	// ---------------- ORDER_PRODUCT -------------------
	ORDER_PRODUCT_QUANTITY_MINIMUM(HttpStatus.BAD_REQUEST, "주문 상품 수량은 1 이상이어야 합니다."),
	// ---------------- PAYMENT -------------------
	PAYMENT_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "취소할 수 없는 결제 상태입니다"),
	PAYMENT_CANCEL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "결제 취소에 실패했습니다"),
	PAYMENT_CONFIRM_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "결제 처리 중 오류가 발생했습니다."),
	PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 요청 금액이 주문 정보와 다릅니다."),
	PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),
	PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 결제입니다."),
	PAYMENT_APPROVE_NOT_ALLOWED(HttpStatus.CONFLICT, "결제 승인이 불가능한 상태입니다."),
	PAYMENT_ALREADY_CANCELLED(HttpStatus.CONFLICT, "이미 취소된 결제입니다."),
	PAYMENT_CANCEL_NOT_ALLOWED(HttpStatus.CONFLICT, "결제 취소가 불가능한 상태입니다."),

	// ---------------- MAIL -------------------
	MAIL_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "인증 메일 전송 중 오류가 발생했습니다."),
	MAIL_CONTENT_BUILD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "인증 메일 내용을 생성하는 중 오류가 발생했습니다."),
	EMAIL_AUTH_CODE_INVALID(HttpStatus.BAD_REQUEST, "잘못된 인증번호입니다."),
	EMAIL_AUTH_CODE_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

	// ---------------- PET -------------------
	PET_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 반려동물을 찾을 수 없습니다."),

	// ---------------- BOARD -------------------
	BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
	BOARD_NOT_WRITER(HttpStatus.FORBIDDEN, "게시글 작성자만 수정/삭제할 수 있습니다."),

	// ---------------- COMMENT -------------------
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
	COMMENT_NOT_WRITER(HttpStatus.FORBIDDEN, "댓글 작성자만 수정/삭제할 수 있습니다.");
	//BOARD_NOT_WRITER(HttpStatus.FORBIDDEN, "게시글 작성자만 수정/삭제할 수 있습니다.");

	private final HttpStatus status;
	private final String message;
}
