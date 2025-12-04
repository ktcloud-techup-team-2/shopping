package com.kt.controller.delivery;

import java.util.List;

import com.kt.common.api.ApiResponseEntity;
import com.kt.security.AuthUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ApiResponseEntity<DeliveryAddressResponse> createAddress(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody @Valid DeliveryAddressRequest.Create request
    ) {
        var response = deliveryAddressService.createAddress(authUser.id(), request);
				return ApiResponseEntity.created(response);
    }

    @GetMapping
    public ApiResponseEntity<List<DeliveryAddressResponse>> getAddressList(@AuthenticationPrincipal AuthUser authUser) {
        var response = deliveryAddressService.getAddressList(authUser.id());
        return ApiResponseEntity.success(response);
    }

    @GetMapping("/{addressId}")
    public ApiResponseEntity<DeliveryAddressResponse> getAddress(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long addressId
    ) {
        var response = deliveryAddressService.getAddress(authUser.id(), addressId);
        return ApiResponseEntity.success(response);
    }

    @PutMapping("/{addressId}")
    public ApiResponseEntity<DeliveryAddressResponse> updateAddress(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long addressId,
            @RequestBody @Valid DeliveryAddressRequest.Update request
    ) {
        var response = deliveryAddressService.updateAddress(authUser.id(), addressId, request);
        return ApiResponseEntity.success(response);
    }

    @PatchMapping("/{addressId}/set-default")
    public ApiResponseEntity<Void> setDefaultAddress(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long addressId
    ) {
        deliveryAddressService.setDefaultAddress(authUser.id(), addressId);
        return ApiResponseEntity.empty();
    }

    @DeleteMapping("/{addressId}")
    public ApiResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long addressId
    ) {
        deliveryAddressService.deleteAddress(authUser.id(), addressId);
        return ApiResponseEntity.empty();
    }
}
