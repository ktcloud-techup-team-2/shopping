package com.kt.dto.payment;

import com.kt.domain.payment.PaymentStatus;
import com.kt.domain.payment.PaymentType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PaymentRequest {

	public record Create(
		@NotNull
		String orderNumber,

		@NotNull
		@Min(0)
		Long deliveryFee,

		@NotNull
		PaymentType type
	) {}

	public record ChangeStatus(
		@NotNull
		PaymentStatus status
	) {}

}
