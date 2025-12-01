package com.kt.controller.payment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	@GetMapping("/{paymentId}")
	public ApiResponseEntity<PaymentResponse.Check> getPayment(@PathVariable Long paymentId) {
		Payment payment = paymentService.getPayment(paymentId);
		return ApiResponseEntity.success(PaymentResponse.Check.from(payment));
	}

	@PatchMapping("/{paymentId}/approve")
	public ApiResponseEntity<PaymentResponse.Check> approvePayment(@PathVariable Long paymentId) {
		Payment payment = paymentService.approvePayment(paymentId);
		return ApiResponseEntity.success(PaymentResponse.Check.from(payment));
	}

	@PatchMapping("/{paymentId}/cancel")
	public ApiResponseEntity<PaymentResponse.Check> cancelPayment(@PathVariable Long paymentId) {
		Payment payment = paymentService.cancelPayment(paymentId);
		return ApiResponseEntity.success(PaymentResponse.Check.from(payment));
	}
}
