package com.kt.service.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.order.Order;
import com.kt.domain.payment.Payment;
import com.kt.dto.payment.PaymentRequest;
import com.kt.repository.order.OrderRepository;
import com.kt.repository.payment.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final OrderRepository orderRepository;

	public Payment createPayment(Long userId, PaymentRequest.Create request) {
		Order order = orderRepository.findByOrderNumber(request.orderNumber())
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

		Payment payment = Payment.create(userId, order, request.deliveryFee(), request.type());
		return paymentRepository.save(payment);
	}

	@Transactional(readOnly = true)
	public Payment getPayment(Long paymentId) {
		return paymentRepository.findById(paymentId)
			.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
	}

	public Payment approvePayment(Long paymentId) {
		Payment payment = paymentRepository.findById(paymentId)
			.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
		payment.approve();
		return payment;
	}

	public Payment cancelPayment(Long paymentId) {
		Payment payment = paymentRepository.findById(paymentId)
			.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
		payment.cancel();
		return payment;
	}
}