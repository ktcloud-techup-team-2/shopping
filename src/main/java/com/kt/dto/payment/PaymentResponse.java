package com.kt.dto.payment;

import java.time.LocalDateTime;

import com.kt.domain.payment.Payment;
import com.kt.domain.payment.PaymentStatus;

public interface PaymentResponse {

	record Create(
		PaymentStatus status,
		Long paymentAmount,
		LocalDateTime createdAt
	){
		public static Create from(Payment payment){
			return new Create(
				payment.getStatus(),
				payment.getPaymentAmount(),
				payment.getCreatedAt()
			);
		}
	}
}
