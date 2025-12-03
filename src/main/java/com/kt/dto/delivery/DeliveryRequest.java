package com.kt.dto.delivery;

import com.kt.domain.delivery.DeliveryStatus;

import jakarta.validation.constraints.NotBlank;
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

	public record UpdateStatus(
		@NotNull(message = "변경할 상태는 필수입니다.")
		DeliveryStatus status,

		String courierCode,
		String trackingNumber
	) {

	}

	public record RegisterTracking(
		@NotBlank(message = "택배사 코드는 필수입니다.")
		String courierCode,

		@NotBlank(message = "송장 번호는 필수입니다.")
		String trackingNumber
	) {

	}
}
