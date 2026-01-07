package com.kt.dto.payment;

import java.time.LocalDateTime;

import com.kt.domain.payment.Payment;
import com.kt.domain.payment.PaymentStatus;
import com.kt.domain.payment.PaymentType;

public class PaymentResponse {

	//결제 승인
	public record ConfirmResult(
		String orderNumber,
		String paymentKey,
		Long amount,
		String status
	) {
		public static ConfirmResult from(Payment payment) {
			return new ConfirmResult(
				payment.getOrderNumber(),
				payment.getPaymentKey(),
				payment.getPaymentAmount(),
				payment.getStatus().name()
			);
		}
	}

	//결제 조회
	public record Detail(
		Long paymentId,
		String orderNumber,
		String paymentKey,
		Long orderAmount,
		Long deliveryFee,
		Long paymentAmount,
		PaymentStatus status,
		String paymentType,
		LocalDateTime createdAt
	) {
		public static Detail from(Payment payment) {
			return new Detail(
				payment.getId(),
				payment.getOrderNumber(),
				payment.getPaymentKey(),
				payment.getOrderAmount(),
				payment.getDeliveryFee(),
				payment.getPaymentAmount(),
				payment.getStatus(),
				payment.getType().name(),
				payment.getCreatedAt()
			);
		}
	}

	//결제 취소
	public record CancelResult(
		Long paymentId,
		String orderNumber,
		PaymentStatus status
	) {
		public static CancelResult from(Payment payment) {
			return new CancelResult(
				payment.getId(),
				payment.getOrderNumber(),
				payment.getStatus()
			);
		}
	}

	public record AdminList(
		Long paymentId,
		Long userId,
		String orderNumber,
		Long paymentAmount,
		PaymentType type,
		PaymentStatus status,
		LocalDateTime createdAt
	){
		public static AdminList from(Payment payment){
			return new AdminList(
				payment.getId(),
				payment.getUserId(),
				payment.getOrderNumber(),
				payment.getPaymentAmount(),
				payment.getType(),
				payment.getStatus(),
				payment.getCreatedAt()
			);
		}
	}

	public record AdminDetail(
		Long paymentId,
		Long userId,
		String orderNumber,
		Long orderAmount,
		Long deliveryFee,
		Long paymentAmount,
		PaymentType type,
		PaymentStatus status,
		LocalDateTime createdAt
	){
		public static AdminDetail from(Payment payment){
			return new AdminDetail(
				payment.getId(),
				payment.getUserId(),
				payment.getOrderNumber(),
				payment.getOrderAmount(),
				payment.getDeliveryFee(),
				payment.getPaymentAmount(),
				payment.getType(),
				payment.getStatus(),
				payment.getCreatedAt()
			);
		}
	}
}
