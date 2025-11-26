package com.kt.service.delivery;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.common.Preconditions;
import com.kt.domain.delivery.Delivery;
import com.kt.dto.delivery.DeliveryRequest;
import com.kt.dto.delivery.DeliveryResponse;
import com.kt.repository.delivery.DeliveryAddressRepository;
import com.kt.repository.delivery.DeliveryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {
	private final DeliveryRepository deliveryRepository;
	private final DeliveryAddressRepository deliveryAddressRepository;

	public DeliveryResponse.Detail createDelivery(DeliveryRequest.Create request){
		var isDeliveryExist = deliveryRepository.existsByOrderId(request.orderId());

		Preconditions.validate(
			!isDeliveryExist,
			ErrorCode.DELIVERY_ALREADY_EXISTS
		);

		var address = deliveryAddressRepository.findById(request.deliveryAddressId())
			.orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_ADDRESS_NOT_FOUND));

		Preconditions.validate(
			address.getIsActive(),
			ErrorCode.DELIVERY_ADDRESS_ALREADY_DELETED
		);

		var delivery = Delivery.create(
			request.orderId(),
			request.deliveryAddressId(),
			request.deliveryFee()
		);

		var savedDelivery = deliveryRepository.save(delivery);
		return DeliveryResponse.Detail.from(savedDelivery, address);
	}
}
