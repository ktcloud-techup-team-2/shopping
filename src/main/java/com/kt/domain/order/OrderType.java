package com.kt.domain.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderType {

	CART("장바구니 주문"),
	DIRECT("바로 주문");

	private final String description;
}

