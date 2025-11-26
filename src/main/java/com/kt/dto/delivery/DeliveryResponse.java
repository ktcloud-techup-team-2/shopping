package com.kt.dto.delivery;

import java.time.LocalDateTime;

import com.kt.domain.delivery.Delivery;
import com.kt.domain.delivery.DeliveryAddress;
import com.kt.domain.delivery.DeliveryStatus;

public interface DeliveryResponse {

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
				delivery.getTrackingNumber(),
				address.getCreatedAt()
			);
		}
	}
}
