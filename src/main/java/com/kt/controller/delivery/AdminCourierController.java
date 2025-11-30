package com.kt.controller.delivery;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.delivery.CourierRequest;
import com.kt.dto.delivery.CourierResponse;
import com.kt.service.delivery.CourierService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/delivery/couriers")
public class AdminCourierController {

	private final CourierService courierService;

	@PostMapping
	public ApiResponseEntity<CourierResponse> createCourier(
		@Valid @RequestBody CourierRequest.Create request
	) {
		var response = courierService.createCourier(request);
		return ApiResponseEntity.created(response);
	}

	@GetMapping
	public ApiResponseEntity<List<CourierResponse>> getCourierList() {
		var response = courierService.getCourierList();
		return ApiResponseEntity.success(response);
	}

	@PutMapping("/{courierId}")
	public ApiResponseEntity<CourierResponse> updateCourier(
		@PathVariable Long courierId,
		@Valid @RequestBody CourierRequest.Update request
	) {
		var response = courierService.updateCourier(courierId, request);
		return ApiResponseEntity.success(response);
	}

	@DeleteMapping("/{courierId}")
	public ApiResponseEntity<Void> deleteCourier(
		@PathVariable Long courierId
	) {
		courierService.deleteCourier(courierId);
		return ApiResponseEntity.empty();
	}
}