package com.kt.dto.order;

import com.kt.domain.order.OrderStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class OrderRequest {

	public record Create(

		@NotNull
		Long productId,

		@NotNull
		String receiverName,

		@NotNull
		String receiverAddress,

		@NotNull
		@Pattern(regexp = "^(0\\d{1,2})-(\\d{3,4})-(\\d{4})$")
		String receiverMobile,

		@Min(value=1)
		@Max(value=999)
		int quantity,

		@NotNull
		Long deliveryAddressId,
		@NotNull
		Integer deliveryFee

	){

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
