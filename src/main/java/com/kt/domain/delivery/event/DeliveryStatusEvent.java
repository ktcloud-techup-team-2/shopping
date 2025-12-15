package com.kt.domain.delivery.event;

import com.kt.domain.delivery.DeliveryStatus;

public record DeliveryStatusEvent(
	Long deliveryId,
	Long orderId,
	DeliveryStatus status,
	String trackingNumber,
	String courierCode
) {
	public static DeliveryStatusEvent of(
		Long deliveryId,
		Long orderId,
		DeliveryStatus status,
		String trackingNumber,
		String courierCode
	) {
		return new DeliveryStatusEvent(deliveryId, orderId, status, trackingNumber, courierCode);
	}
}