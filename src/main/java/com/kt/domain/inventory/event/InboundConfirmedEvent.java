package com.kt.domain.inventory.event;

import java.time.LocalDateTime;

public record InboundConfirmedEvent(
	String eventId,
	Long productId,
	long quantity,
	LocalDateTime confirmedAt
) {
	public static InboundConfirmedEvent of(
		String eventId,
		Long productId,
		long quantity,
		LocalDateTime confirmedAt
	) {
		return new InboundConfirmedEvent(eventId, productId, quantity, confirmedAt);
	}
}
