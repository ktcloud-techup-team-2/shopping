package com.kt.controller.payment;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.common.api.ApiResponseEntity;
import com.kt.domain.payment.Payment;
import com.kt.dto.payment.PaymentRequest;
import com.kt.dto.payment.PaymentResponse;
import com.kt.security.AuthUser;
import com.kt.service.payment.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	//최종 결제 승인 : 프론트엔드가 토스 결제창 인증 후 받은 정보 전달
	@PostMapping("/confirm")
	public ApiResponseEntity<PaymentResponse.ConfirmResult> confirmPayment(
		@AuthenticationPrincipal AuthUser authUser,
		@RequestBody @Valid PaymentRequest.Confirm request
	) {
		// 결제 승인 로직 실행
		Payment payment = paymentService.confirmPayment(authUser.id(), request);

		// 성공 결과 반환
		return ApiResponseEntity.success(PaymentResponse.ConfirmResult.from(payment));
	}

	@GetMapping("/{paymentId}")
	public ApiResponseEntity<PaymentResponse.Check> getPayment(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Long paymentId
	) {
		Payment payment = paymentService.getPayment(paymentId);
		return ApiResponseEntity.success(PaymentResponse.Check.from(payment));
	}

	@PatchMapping("/{paymentId}/cancel")
	public ApiResponseEntity<PaymentResponse.Check> cancelPayment(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Long paymentId
	) {
		Payment payment = paymentService.cancelPayment(paymentId);
		return ApiResponseEntity.success(PaymentResponse.Check.from(payment));
	}
}
