package com.kt.controller.payment;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kt.common.api.ApiResponseEntity;
import com.kt.domain.payment.Payment;
import com.kt.dto.payment.PaymentRequest;
import com.kt.dto.payment.PaymentResponse;
import com.kt.service.payment.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	public ApiResponseEntity<PaymentResponse.Create> createPayment(
		@RequestParam Long userId,
		@RequestBody @Valid PaymentRequest.Create request
	){
		Payment payment = paymentService.createPayment(userId, request);
		return ApiResponseEntity.created(PaymentResponse.Create.from(payment));
	}
}
