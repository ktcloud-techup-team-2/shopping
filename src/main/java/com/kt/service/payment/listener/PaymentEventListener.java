package com.kt.service.payment.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.kt.domain.payment.event.PaymentConfirmedEvent;
import com.kt.service.order.OrderService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

	private final OrderService orderService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePaymentConfirmed(PaymentConfirmedEvent event) {
		orderService.completeOrder(event.userId(), event.orderNumber());
	}
}

