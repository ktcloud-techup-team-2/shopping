package com.kt.domain.delivery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryStatus {
	PENDING("주문접수", "주문이 접수되었습니다"),
	PREPARING("상품준비중", "상품을 준비하고 있습니다"),
	READY("출고준비완료", "상품이 출고 준비되었습니다"),
	SHIPPING("배송중", "상품이 배송중입니다"),
	DELIVERED("배송완료", "배송이 완료되었습니다"),
	CANCELLED("배송취소", "배송이 취소되었습니다");

	private final String title;
	private final String description;
}
