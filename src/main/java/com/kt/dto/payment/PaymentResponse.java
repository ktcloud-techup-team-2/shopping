package com.kt.dto.payment;

import java.time.LocalDateTime;

import com.kt.domain.payment.Payment;
import com.kt.domain.payment.PaymentStatus;
import com.kt.domain.payment.PaymentType;

public interface PaymentResponse {

	record Create(
		Long paymentId,
		PaymentStatus status,
		Long paymentAmount,
		LocalDateTime createdAt
	){
		public static Create from(Payment payment){
			return new Create(
				payment.getId(),
				payment.getStatus(),
				payment.getPaymentAmount(),
				payment.getCreatedAt()
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
