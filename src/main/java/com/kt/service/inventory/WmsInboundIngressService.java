package com.kt.service.inventory;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import com.kt.common.Preconditions;
import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.inventory.event.InboundConfirmedEvent;
import com.kt.dto.wms.InboundConfirmedRequest;
import com.kt.dto.wms.InboundConfirmedResponse;
import com.kt.repository.product.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WmsInboundIngressService {

	private final ProductRepository productRepository;
	private final ApplicationEventPublisher eventPublisher;

	public InboundConfirmedResponse confirmInbound(InboundConfirmedRequest request) {
		Preconditions.validate(
			request.quantity() != null && request.quantity() > 0,
			ErrorCode.WMS_INBOUND_EVENT_QUANTITY_INVALID
		);

		var product = productRepository.findByIdAndDeletedFalse(request.productId())
			.orElseThrow(() -> new CustomException(ErrorCode.WMS_INBOUND_PRODUCT_NOT_FOUND));

		LocalDateTime confirmedAt = LocalDateTime.now();
		eventPublisher.publishEvent(InboundConfirmedEvent.of(
			request.eventId(),
			product.getId(),
			request.quantity(),
			confirmedAt
		));

		return InboundConfirmedResponse.of(
			request.eventId(),
			product.getId(),
			request.quantity(),
			confirmedAt
		);
	}
}