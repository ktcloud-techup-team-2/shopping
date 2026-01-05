package com.kt.domain.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {


	// 주문 생성됨
	PENDING("결제대기"),

	// 결제 완료, 주문 확정
	COMPLETED("주문완료"),

	// 배송
	SHIPPED("배송중"),
	DELIVERED("배송완료"),

	// 주문 취소
	CANCELLED("주문취소");

	private final String description;

}