package com.kt.dto.delivery;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class DeliveryRequest {
	public record Create(
		@NotNull(message = "주문 ID는 필수입니다.")
		Long orderId,

		@NotNull(message = "배송지 ID는 필수입니다.")
		Long deliveryAddressId,

		@PositiveOrZero(message = "배송비는 0 이상이어야 합니다. (무료배송 또는 배송비 부과)")
		Integer deliveryFee
	) {

	}
}
