package com.kt.service.delivery;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.common.Preconditions;
import com.kt.domain.delivery.Delivery;
import com.kt.domain.delivery.DeliveryStatus;
import com.kt.domain.delivery.DeliveryStatusHistory;
import com.kt.domain.delivery.event.DeliveryStatusEvent;
import com.kt.dto.delivery.DeliveryRequest;
import com.kt.dto.delivery.DeliveryResponse;
import com.kt.repository.delivery.CourierRepository;
import com.kt.repository.delivery.DeliveryAddressRepository;
import com.kt.repository.delivery.DeliveryRepository;
import com.kt.repository.delivery.DeliveryStatusHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {
	private final DeliveryRepository deliveryRepository;
	private final DeliveryAddressRepository deliveryAddressRepository;
	private final DeliveryStatusHistoryRepository deliveryStatusHistoryRepository;
	private final CourierRepository courierRepository;

	private final ApplicationEventPublisher eventPublisher;

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

		saveHistory(savedDelivery.getId(), DeliveryStatus.PENDING);

		return DeliveryResponse.Detail.from(savedDelivery, address);
	}

	public DeliveryResponse.Detail getDeliveryByOrderId(Long orderId){
		var delivery = deliveryRepository.findByOrderId(orderId)
			.orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));

		var address = deliveryAddressRepository.findById(delivery.getDeliveryAddressId())
			.orElseThrow(() ->  new CustomException(ErrorCode.DELIVERY_ADDRESS_NOT_FOUND));

		return DeliveryResponse.Detail.from(delivery, address);
	}

	public DeliveryResponse.Tracking trackDelivery(String trackingNumber){
		var delivery = deliveryRepository.findByTrackingNumber(trackingNumber)
			.orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));

		return DeliveryResponse.Tracking.from(delivery);
	}

	public Page<DeliveryResponse.Simple> getDeliveryList(Pageable pageable){
		return deliveryRepository.findAll(pageable)
			.map(DeliveryResponse.Simple::from);
	}

	public DeliveryResponse.Detail getDeliveryDetail(Long deliveryId){
		var delivery = deliveryRepository.findById(deliveryId)
			.orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));

		var address = deliveryAddressRepository.findById(delivery.getDeliveryAddressId())
			.orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_ADDRESS_NOT_FOUND));

		return DeliveryResponse.Detail.from(delivery, address);
	}

	public DeliveryResponse.Detail updateDeliveryStatus(Long deliveryId, DeliveryRequest.UpdateStatus request) {
		var delivery = deliveryRepository.findById(deliveryId)
			.orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));

		switch (request.status()) {
			case PREPARING -> delivery.startPreparing();
			case READY -> delivery.readyForShipment();
			case SHIPPING -> {
				if (request.trackingNumber() == null || request.courierCode() == null) {
					throw new CustomException(ErrorCode.COMMON_INVALID_ARGUMENT);
				}

				boolean isValidCourier = courierRepository.existsByCode(request.courierCode());
				Preconditions.validate(isValidCourier, ErrorCode.COURIER_NOT_FOUND);

				delivery.updateTrackingInfo(request.courierCode(), request.trackingNumber());
				delivery.ship();
			}
			case DELIVERED -> delivery.complete();
			case CANCELLED -> delivery.cancel();
			default -> throw new CustomException(ErrorCode.COMMON_INVALID_ARGUMENT);
		}

		saveHistory(deliveryId, request.status());

		eventPublisher.publishEvent(DeliveryStatusEvent.of(
			delivery.getId(),
			delivery.getOrderId(),
			delivery.getStatus(),
			delivery.getTrackingNumber(),
			delivery.getCourierCode()
		));

		var address =  deliveryAddressRepository.findById(delivery.getDeliveryAddressId())
			.orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_ADDRESS_NOT_FOUND));

		return DeliveryResponse.Detail.from(delivery, address);
	}

	private void saveHistory(Long deliveryId, DeliveryStatus status) {
		var history = DeliveryStatusHistory.create(deliveryId, status);
		deliveryStatusHistoryRepository.save(history);
	}
}
