package com.kt.dto.wms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InboundConfirmedRequest(
	@NotBlank(message = "이벤트 ID는 필수입니다.")
	String eventId,

	@NotNull(message = "제품 ID는 필수입니다.")
	Long productId,

	@NotNull(message = "수량은 필수입니다.")
	Long quantity
) {
}
