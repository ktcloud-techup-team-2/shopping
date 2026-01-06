package com.kt.service.payment;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.order.Order;
import com.kt.domain.payment.Payment;
import com.kt.domain.payment.PaymentType;
import com.kt.domain.payment.event.PaymentConfirmedEvent;
import com.kt.dto.payment.PaymentRequest;
import com.kt.repository.payment.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final ApplicationEventPublisher eventPublisher;
	// 실제 연동 시 RestTemplate 등이 필요

	public void createReadyPayment(Long userId, Order order, Long amount, String paymentTypeStr) {

		PaymentType type = PaymentType.valueOf(paymentTypeStr);

		Payment payment = Payment.create(
			userId,
			order,
			amount,
			type
		);

		paymentRepository.save(payment);

	}

	public Payment confirmPayment(Long userId, PaymentRequest.Confirm request) {

		//orderNumber로 DB에 저장된 결제 대기(READY) 데이터 조회
		Payment payment = paymentRepository.findByOrderNumber(request.orderNumber())
			.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

		//중복 결제 승인 방지 (READY 상태만 승인 가능)
		if (!payment.isReady()) {
			throw new CustomException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
		}

		//결제 금액 검증 (요청 금액과 DB 저장 금액 비교)
		if (!payment.getPaymentAmount().equals(request.amount())) {
			throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
		}

		try {
			/**
			 * [외부 API 호출 모방]
			 * 실제로는 여기서 RestTemplate을 이용해 토스 서버로 승인 요청을 보냄
			 * 성공했다는 가정하에 진행
			 */
			boolean isExternalApiSuccess = true;

			if (!isExternalApiSuccess) {
				//결제 실패 = api 호출 실패 or 금액 불일치 등의 이유로
				payment.failPayment();
				throw new CustomException(ErrorCode.PAYMENT_CONFIRM_FAILED);
			}

			// 상태 변경 (READY -> DONE)
			payment.confirmPayment(request.paymentKey());

			// 결제 완료 이벤트 발행 -> 주문 완료 + 재고 차감
			eventPublisher.publishEvent(PaymentConfirmedEvent.of(userId, payment.getOrderNumber()));

			return payment;

		} catch (Exception e) {
			//결제 실패
			payment.failPayment();
			throw new CustomException(ErrorCode.PAYMENT_CONFIRM_FAILED);
		}
	}

	//결제 조회
	@Transactional(readOnly = true)
	public Payment getPayment(Long userId, Long paymentId) {

		return paymentRepository.findByIdAndUserId(paymentId, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
	}


	//결제 취소
	public Payment cancelPayment(Long userId, Long paymentId, String cancelReason) {

		Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

		// 취소 가능 상태 체크
		payment.isCancelable();

		try {
			//토스 취소 api 호출
			boolean isExternalApiSuccess = true;

			if (!isExternalApiSuccess) {
				throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
			}

			//상태 변경 (DONE -> CANCELED) 및 취소 사유 저장
			payment.cancel(cancelReason);

			return payment;

		} catch (Exception e) {
			throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
		}
	}
}