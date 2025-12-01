package com.kt.dto.delivery;

import com.kt.domain.delivery.Courier;

public record CourierResponse(
	Long id,
	String code,
	String name,
	Boolean isActive
) {
	public static CourierResponse from(Courier courier) {
		return new CourierResponse(
			courier.getId(),
			courier.getCode(),
			courier.getName(),
			courier.getIsActive()
		);
	}
}