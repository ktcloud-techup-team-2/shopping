package com.kt.dto.payment;

import com.kt.domain.payment.PaymentStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentRequest {

	//결제 승인
	public record Confirm(

		@NotNull(message = "결제 키는 필수입니다.")
		String paymentKey,
		@NotNull(message = "주문 번호는 필수입니다.")
		String orderNumber,
		@NotNull(message = "결제 금액은 필수입니다.")
		Long amount
	) {

	}

	//결제 취소
	public record Cancel(
		@NotBlank(message = "취소 사유는 필수입니다")
		String cancelReason
	) {}

	public record ChangeStatus(
		@NotNull
		PaymentStatus status
	) {}

}
