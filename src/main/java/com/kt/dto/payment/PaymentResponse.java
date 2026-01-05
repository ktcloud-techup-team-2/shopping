package com.kt.dto.payment;

import java.time.LocalDateTime;

import com.kt.domain.payment.Payment;
import com.kt.domain.payment.PaymentStatus;
import com.kt.domain.payment.PaymentType;

public interface PaymentResponse {

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

	record Check(
		Long paymentId,
		String orderNumber,
		Long orderAmount,
		Long deliveryFee,
		Long paymentAmount,
		PaymentType type,
		PaymentStatus status,
		LocalDateTime createdAt
	){
		public static Check from(Payment payment){
			return new Check(
				payment.getId(),
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

	record AdminList(
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

	record AdminDetail(
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
