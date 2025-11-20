package com.kt.dto.delivery;

import java.time.LocalDateTime;

import com.kt.entity.delivery.DeliveryAddress;

public record DeliveryAddressResponse(
	Long id,
	String addressName,
	String receiverName,
	String receiverMobile,
	String postalCode,
	String roadAddress,
	String detailAddress,
	Boolean isDefault,
	Boolean isActive,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static DeliveryAddressResponse from(DeliveryAddress deliveryAddress) {
		return new DeliveryAddressResponse(
			deliveryAddress.getId(),
			deliveryAddress.getAddressName(),
			deliveryAddress.getReceiverName(),
			deliveryAddress.getReceiverMobile(),
			deliveryAddress.getPostalCode(),
			deliveryAddress.getRoadAddress(),
			deliveryAddress.getDetailAddress(),
			deliveryAddress.getIsDefault(),
			deliveryAddress.getIsActive(),
			deliveryAddress.getCreatedAt(),
			deliveryAddress.getUpdatedAt()
		);
	}
}
