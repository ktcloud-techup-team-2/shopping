package com.kt.service.delivery;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

		if (currentCount >= MAX_ADDRESS_COUNT) {
			throw new IllegalArgumentException("배송지는 최대 " +  MAX_ADDRESS_COUNT + "개까지 등록 가능합니다.");
		}

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
		var address = deliveryAddressRepository.findByIdAndUserId(addressId, userId)
			.orElseThrow(()-> new IllegalArgumentException("배송지를 찾을 수 없습니다."));

		if(!address.getIsActive()) {
			throw new IllegalArgumentException("삭제된 배송지 입니다.");
		}

		return DeliveryAddressResponse.from(address);
	}

	// 기본 배송지 조회
	public DeliveryAddressResponse getDefaultAddress(Long userId) {
		var address = deliveryAddressRepository.findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId)
			.orElseThrow(()-> new IllegalArgumentException("기본 배송지가 설정되지 않았습니다."));

		return DeliveryAddressResponse.from(address);
	}

	// 배송지 수정
	public DeliveryAddressResponse updateAddress(Long userId, Long addressId, DeliveryAddressRequest.Update request) {
		var address = deliveryAddressRepository.findByIdAndUserId(addressId, userId)
			.orElseThrow(() -> new IllegalArgumentException("배송지를 찾을 수 없습니다."));

		if(!address.getIsActive()) {
			throw new IllegalArgumentException("삭제된 배송지는 수정할 수 없습니다.");
		}

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

		var address = deliveryAddressRepository.findByIdAndUserId(addressId, userId)
			.orElseThrow(() -> new IllegalArgumentException("배송지를 찾을 수 없습니다."));

		if(!address.getIsActive()) {
			throw new IllegalArgumentException("삭제된 배송지는 기본 배송지로 설정할 수 없습니다.");
		}

		address.setAsDefault();
	}

	// 배송지 삭제
	public void deleteAddress(Long userId, Long addressId) {
		var address = deliveryAddressRepository.findByIdAndUserId(addressId, userId)
			.orElseThrow(() -> new IllegalArgumentException("배송지를 찾을 수 없습니다."));

		if(!address.getIsActive()) {
			throw new IllegalArgumentException("이미 삭제된 배송지입니다.");
		}

		if(address.getIsDefault()) {
			address.unsetAsDefault();
		}

		address.deactivate();
	}
}
