package com.kt.dto.order;

import com.kt.domain.order.OrderStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class OrderRequest {

	// 장바구니 주문 요청
	public record CartOrder(

		@NotNull
		String receiverName,

		@NotNull
		String receiverAddress,

		@NotNull
		@Pattern(regexp = "^(0\\d{1,2})-(\\d{3,4})-(\\d{4})$")
		String receiverMobile,

		@NotNull
		Long deliveryAddressId,

		@NotNull
		Integer deliveryFee
	) {
	}

	// 바로 주문 요청
	public record DirectOrder(
		@NotNull
		Long productId,

		@Min(value = 1)
		int quantity,

		@NotNull
		String receiverName,

		@NotNull
		String receiverAddress,

		@NotNull
		@Pattern(regexp = "^(0\\d{1,2})-(\\d{3,4})-(\\d{4})$")
		String receiverMobile,

		@NotNull
		Long deliveryAddressId,

		@NotNull
		Integer deliveryFee
	) {
	}

	public record Update(

		@NotNull
		String receiverName,

		@NotNull
		String receiverAddress,

		@NotNull
		@Pattern(regexp = "^(0\\d{1,2})-(\\d{3,4})-(\\d{4})$")
		String receiverMobile

	){

	}

	// 관리자 주문 상태 변경
	public record ChangeStatus(
		@NotNull
		OrderStatus status
	){

	}

}
