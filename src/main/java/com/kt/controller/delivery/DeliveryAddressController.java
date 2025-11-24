package com.kt.controller.delivery;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.dto.delivery.DeliveryAddressRequest;
import com.kt.dto.delivery.DeliveryAddressResponse;
import com.kt.service.delivery.DeliveryAddressService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/delivery/addresses")
@RequiredArgsConstructor
public class DeliveryAddressController {
	private final DeliveryAddressService deliveryAddressService;

	@PostMapping
	public ResponseEntity<DeliveryAddressResponse> createAddress(
		// @AuthenticationPrincipal CurrentUser currentUser
		Long userId,
		@RequestBody @Valid DeliveryAddressRequest.Create request
	) {
		var response = deliveryAddressService.createAddress(userId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<DeliveryAddressResponse>> getAddressList(Long userId) {
		var response = deliveryAddressService.getAddressList(userId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{addressId}")
	public ResponseEntity<DeliveryAddressResponse> getAddress(
		Long userId,
		@PathVariable Long addressId
	) {
		var response = deliveryAddressService.getAddress(userId, addressId);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{addressId}")
	public ResponseEntity<DeliveryAddressResponse> updateAddress(
		Long userId,
		@PathVariable Long addressId,
		@RequestBody @Valid DeliveryAddressRequest.Update request
	) {
		var response = deliveryAddressService.updateAddress(userId, addressId, request);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{addressId}/set-default")
	public ResponseEntity<Void> setDefaultAddress(
		Long userId,
		@PathVariable Long addressId
	) {
		deliveryAddressService.setDefaultAddress(userId, addressId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{addressId}")
	public ResponseEntity<Void> deleteAddress(
		Long userId,
		@PathVariable Long addressId
	) {
		deliveryAddressService.deleteAddress(userId, addressId);
		return ResponseEntity.noContent().build();
	}
}
