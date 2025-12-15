package com.kt.controller.delivery;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.delivery.DeliveryRequest;
import com.kt.dto.delivery.DeliveryResponse;
import com.kt.service.delivery.DeliveryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/delivery")
public class DeliveryController {

	private final DeliveryService deliveryService;

	@PostMapping
	public ApiResponseEntity<DeliveryResponse.Detail> createDelivery(
		@Valid @RequestBody DeliveryRequest.Create request
	) {
		var response = deliveryService.createDelivery(request);
		return ApiResponseEntity.created(response);
	}

	@GetMapping("/orders/{orderId}/status")
	public ApiResponseEntity<DeliveryResponse.Detail> getDeliveryStatus(
		@PathVariable Long orderId
	) {
		var response = deliveryService.getDeliveryByOrderId(orderId);
		return ApiResponseEntity.success(response);
	}

	@GetMapping("/tracking/{trackingNumber}")
	public ApiResponseEntity<DeliveryResponse.Tracking> trackDelivery(
		@PathVariable String trackingNumber
	) {
		var response = deliveryService.trackDelivery(trackingNumber);
		return ApiResponseEntity.success(response);
	}

}
