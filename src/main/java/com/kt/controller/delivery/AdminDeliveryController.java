package com.kt.controller.delivery;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kt.common.api.ApiResponseEntity;
import com.kt.domain.delivery.DeliveryStatus;
import com.kt.dto.delivery.DeliveryRequest;
import com.kt.dto.delivery.DeliveryResponse;
import com.kt.service.delivery.DeliveryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/delivery/orders")
public class AdminDeliveryController {
	private final DeliveryService deliveryService;

	@GetMapping
	public ApiResponseEntity<List<DeliveryResponse.Simple>> getDeliveryList(
		@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ApiResponseEntity.pageOf(deliveryService.getDeliveryList(pageable));
	}

	@GetMapping("/{deliveryId}")
	public ApiResponseEntity<DeliveryResponse.Detail> getDeliveryDetail(
		@PathVariable Long deliveryId
	) {
		var response = deliveryService.getDeliveryDetail(deliveryId);
		return ApiResponseEntity.success(response);
	}

	@PatchMapping("/{deliveryId}/status")
	public ApiResponseEntity<DeliveryResponse.Detail> updateDeliveryStatus(
		@PathVariable Long deliveryId,
		@Valid @RequestBody DeliveryRequest.UpdateStatus request
	) {
		var response = deliveryService.updateDeliveryStatus(deliveryId, request);
		return ApiResponseEntity.success(response);
	}
}
