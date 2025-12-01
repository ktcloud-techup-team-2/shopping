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

	public Payment createPayment(
		Long userId,
		PaymentRequest.Create request
	){
		// orderNumber로 Order 조회
		Order order = orderRepository.findByOrderNumber(request.orderNumber())
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

		Payment payment = Payment.create(
			userId,
			order,
			request.deliveryFee(),
			order.getOrderNumber(),
			order.getTotalPaymentAmount(),
			request.type()
		);
		return paymentRepository.save(payment);
	}
}
