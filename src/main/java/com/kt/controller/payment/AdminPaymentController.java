package com.kt.controller.payment;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.common.api.ApiResponseEntity;
import com.kt.domain.payment.Payment;
import com.kt.dto.payment.PaymentRequest;
import com.kt.dto.payment.PaymentResponse;
import com.kt.service.payment.AdminPaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

	private final AdminPaymentService adminPaymentService;

	//결제 내역 전체 조회
	@GetMapping
	public ApiResponseEntity<List<PaymentResponse.AdminList>> allPayments() {
		List<Payment> payments = adminPaymentService.allPayments();
		List<PaymentResponse.AdminList> response = payments.stream()
			.map(PaymentResponse.AdminList::from)
			.toList();
		return ApiResponseEntity.success(response);
	}

	//결제 상태 변경
	@PatchMapping("/{paymentId}/status")
	public ApiResponseEntity<PaymentResponse.AdminDetail> changeStatus(
		@PathVariable Long paymentId,
		@RequestBody @Valid PaymentRequest.ChangeStatus request
	) {
		Payment payment = adminPaymentService.changeStatus(paymentId, request);
		return ApiResponseEntity.success(PaymentResponse.AdminDetail.from(payment));
	}
}

