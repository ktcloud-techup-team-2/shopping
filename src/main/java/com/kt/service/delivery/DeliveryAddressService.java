package com.kt.service.delivery;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.CustomException;
import com.kt.common.ErrorCode;
import com.kt.common.Preconditions;
import com.kt.dto.delivery.DeliveryAddressRequest;
import com.kt.dto.delivery.DeliveryAddressResponse;
import com.kt.domain.delivery.DeliveryAddress;
import com.kt.repository.delivery.DeliveryAddressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryAddressService {
	private final DeliveryAddressRepository deliveryAddressRepository;
	private static final int MAX_ADDRESS_COUNT = 10;

	// 배송지 생성
	public DeliveryAddressResponse createAddress(Long userId, DeliveryAddressRequest.Create request) {
		var currentCount = deliveryAddressRepository.countByUserIdAndIsActiveTrue(userId);

		Preconditions.validate(
			currentCount < MAX_ADDRESS_COUNT,
			ErrorCode.DELIVERY_ADDRESS_MAX_COUNT_EXCEEDED
		);

		if(Boolean.TRUE.equals(request.isDefault())) {
			deliveryAddressRepository.findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId)
				.ifPresent(DeliveryAddress::unsetAsDefault);
		}

		var newAddress = DeliveryAddress.from(userId, request);
		var savedAddress = deliveryAddressRepository.save(newAddress);

		return DeliveryAddressResponse.from(savedAddress);
	}

	// 배송지 목록 조회
	public List<DeliveryAddressResponse> getAddressList(Long userId) {
		return deliveryAddressRepository.findByUserIdAndIsActiveTrue(userId)
			.stream()
			.map(DeliveryAddressResponse::from)
			.collect(Collectors.toList());
	}

	// 배송지 단건 조회
	public DeliveryAddressResponse getAddress(Long userId, Long addressId) {
		var address = findAddressByIdAndUserId(addressId, userId);

		Preconditions.validate(address.getIsActive(), ErrorCode.DELIVERY_ADDRESS_ALREADY_DELETED);

		return DeliveryAddressResponse.from(address);
	}

	// 기본 배송지 조회
	public DeliveryAddressResponse getDefaultAddress(Long userId) {
		var address = deliveryAddressRepository.findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId)
			.orElseThrow(()-> new CustomException(ErrorCode.DEFAULT_DELIVERY_ADDRESS_NOT_SET));

		return DeliveryAddressResponse.from(address);
	}

	// 배송지 수정
	public DeliveryAddressResponse updateAddress(Long userId, Long addressId, DeliveryAddressRequest.Update request) {
		var address = findAddressByIdAndUserId(addressId, userId);

		Preconditions.validate(
			address.getIsActive(),
			ErrorCode.DELIVERY_ADDRESS_DELETED_CANNOT_UPDATE
		);

		address.update(
			request.addressName(),
			request.receiverName(),
			request.receiverMobile(),
			request.postalCode(),
			request.roadAddress(),
			request.detailAddress()
		);

		return DeliveryAddressResponse.from(address);
	}

	// 기본 배송지 설정
	public void setDefaultAddress(Long userId, Long addressId) {
		deliveryAddressRepository.findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId)
			.ifPresent(DeliveryAddress::unsetAsDefault);

		var address = findAddressByIdAndUserId(addressId, userId);

		Preconditions.validate(
			address.getIsActive(),
			ErrorCode.DELIVERY_ADDRESS_DELETED_CANNOT_SET_DEFAULT
		);

		address.setAsDefault();
	}

	// 배송지 삭제
	public void deleteAddress(Long userId, Long addressId) {
		var address = findAddressByIdAndUserId(addressId, userId);

		Preconditions.validate(
			address.getIsActive(),
			ErrorCode.DELIVERY_ADDRESS_ALREADY_DELETED
		);

		if(address.getIsDefault()) {
			address.unsetAsDefault();
		}

		address.deactivate();
	}

	private DeliveryAddress findAddressByIdAndUserId(Long addressId, Long userId) {
		return deliveryAddressRepository.findByIdAndUserId(addressId, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_ADDRESS_NOT_FOUND));
	}
}
