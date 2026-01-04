package com.kt.domain.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {


	//주문 flow
	//1. 결제가 완료된 경우
	//주문생성(결제대기) -> 결제 승인 -> 주문완료 -> 배송중 -> 배송완료
	//2. 결제가 실패된 경우
	//주문생성 -> 결제 실패
	//3. 주문을 취소할 경우
	//주문생성 -> 결제 승인 -> 주문 완료 -> 사용자가 주문 취소 클릭 -> 취소 사유 입력 -> 환불

	PENDING("결제대기"),
	COMPLETED("주문완료"),
	SHIPPED("배송중"),
	DELIVERED("배송완료"),
	CANCELLED("주문취소");

	private final String description;

}