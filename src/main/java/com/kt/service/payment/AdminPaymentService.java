package com.kt.service.payment;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.payment.Payment;
import com.kt.dto.payment.PaymentRequest;
import com.kt.repository.payment.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminPaymentService {

	private final PaymentRepository paymentRepository;

	//결제 내역 전체 조회
	public List<Payment> allPayments() {
		return paymentRepository.findAll();
	}

	//결제 상태 변경
	public Payment changeStatus(Long paymentId, PaymentRequest.ChangeStatus request) {
		Payment payment = paymentRepository.findById(paymentId)
			.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
		payment.changeStatus(request.status());
		return payment;
	}
}

