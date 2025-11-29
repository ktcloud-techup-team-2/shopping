package com.kt.dto.delivery;

import java.time.LocalDateTime;

import com.kt.domain.delivery.Delivery;
import com.kt.domain.delivery.DeliveryAddress;
import com.kt.domain.delivery.DeliveryStatus;

public interface DeliveryResponse {

	record Simple(
		Long deliveryId,
		Long orderId,
		String trackingNumber,
		String courierCode,
		DeliveryStatus status,
		LocalDateTime createdAt
	) {
		public static Simple from(Delivery delivery) {
			return new Simple(
				delivery.getId(),
				delivery.getOrderId(),
				delivery.getTrackingNumber(),
				delivery.getCourierCode(),
				delivery.getStatus(),
				delivery.getCreatedAt()
			);
		}
	}

	record Tracking (
		Long deliveryId,
		String courierCode,
		String trackingNumber,
		DeliveryStatus status,
		LocalDateTime shippedAt,
		LocalDateTime deliveredAt
	) {
		public static Tracking from(Delivery delivery) {
			return new Tracking(
				delivery.getId(),
				delivery.getCourierCode(),
				delivery.getTrackingNumber(),
				delivery.getStatus(),
				delivery.getShippedAt(),
				delivery.getDeliveredAt()
			);
		}

	}

	record Detail(
		Long deliveryId,
		Long orderId,

		String receiverName,
		String receiverMobile,

		String PostalCode,
		String roadAddress,
		String detailAddress,

		Integer deliveryFee,
		DeliveryStatus status,
		String courierCode,
		String trackingNumber,
		LocalDateTime createdAt
	) {
		public static Detail from(Delivery delivery, DeliveryAddress address) {
			return new Detail(
				delivery.getId(),
				delivery.getOrderId(),
				address.getReceiverName(),
				address.getReceiverMobile(),
				address.getPostalCode(),
				address.getRoadAddress(),
				address.getDetailAddress(),
				delivery.getDeliveryFee(),
				delivery.getStatus(),
				delivery.getCourierCode(),
				delivery.getTrackingNumber(),
				address.getCreatedAt()
			);
		}
	}
}
