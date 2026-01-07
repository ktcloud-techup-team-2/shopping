package com.kt.domain.payment.event;

public record PaymentConfirmedEvent(
	Long userId,
	String orderNumber
) {
	public static PaymentConfirmedEvent of(Long userId, String orderNumber) {
		return new PaymentConfirmedEvent(userId, orderNumber);
	}
}

