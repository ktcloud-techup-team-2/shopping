package com.kt.dto.wms;

import java.time.LocalDateTime;

public record InboundConfirmedResponse(
	String eventId,
	Long productId,
	long quantity,
	LocalDateTime confirmedAt
) {
	public static InboundConfirmedResponse of(
		String eventId,
		Long productId,
		long quantity,
		LocalDateTime confirmedAt
	) {
		return new InboundConfirmedResponse(
			eventId,
			productId,
			quantity,
			confirmedAt
		);
	}
}