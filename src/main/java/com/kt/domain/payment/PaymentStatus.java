package com.kt.domain.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

	READY("결제대기"),
	IN_PROGRESS("결제 잔행 중"),
	DONE("결제 완료"),
	CANCELED("결제 취소"),
	REFUNDED("환불 완료"),
	FAILED("결제 실패");

	private final String description;
}
