package com.kt.dto.delivery;

import java.time.LocalDateTime;

import com.kt.domain.delivery.DeliveryStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeliveryStatusWebhookRequest(
	@NotBlank(message = "송장번호는 필수입니다.")
	String trackingNumber,

	@NotNull(message = "배송 상태는 필수입니다.")
	DeliveryStatus status,

	@NotNull(message = "이벤트 발생 시간은 필수입니다.")
	LocalDateTime eventTime
) {
}
