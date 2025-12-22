package com.kt.controller.delivery;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.delivery.DeliveryStatusWebhookRequest;
import com.kt.service.delivery.DeliveryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/webhook/delivery")
@RequiredArgsConstructor
public class DeliveryWebhookController {
	private final DeliveryService deliveryService;

	@PostMapping("/status")
	public ApiResponseEntity<Void> receiveDeliveryStatus(@RequestBody DeliveryStatusWebhookRequest request) {
		deliveryService.updateStatusByWebhook(
			request.trackingNumber(),
			request.status()
		);
		return ApiResponseEntity.success();
	}
}
