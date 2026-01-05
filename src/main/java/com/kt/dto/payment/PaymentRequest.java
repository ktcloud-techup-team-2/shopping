package com.kt.dto.payment;

import com.kt.domain.payment.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public class PaymentRequest {

	//결제 승인
	public record Confirm(
		@NotNull String paymentKey,
		@NotNull String orderNumber,
		@NotNull Long amount
	) {

	}

	public record ChangeStatus(
		@NotNull
		PaymentStatus status
	) {}

}
