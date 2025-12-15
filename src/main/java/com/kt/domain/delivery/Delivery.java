package com.kt.domain.delivery;

import java.time.LocalDateTime;

import com.kt.common.Preconditions;
import com.kt.common.api.ErrorCode;
import com.kt.common.jpa.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Delivery extends BaseTimeEntity {

	@Column(nullable = false, unique = true)
	private Long orderId;

	@Column(nullable = false)
	private Long deliveryAddressId;

	@Enumerated(EnumType.STRING)
	private DeliveryStatus status = DeliveryStatus.PENDING;

	private Integer deliveryFee = 0;
	private String deliveryMethod;
	private String courierCode;
	private String trackingNumber;
	private LocalDateTime shippedAt;
	private LocalDateTime deliveredAt;

	public static Delivery create(Long orderId, Long deliveryAddressId, Integer deliveryFee) {
		Delivery delivery = new Delivery();
		delivery.orderId = orderId;
		delivery.deliveryAddressId = deliveryAddressId;
		delivery.deliveryFee = deliveryFee != null ? deliveryFee : 0;
		delivery.status = DeliveryStatus.PENDING;
		return delivery;
	}

	public void updateTrackingInfo(String courierCode, String trackingNumber) {
		this.courierCode = courierCode;
		this.trackingNumber = trackingNumber;
	}

	public void startPreparing() {
		Preconditions.validate(
			this.status == DeliveryStatus.PENDING,
			ErrorCode.DELIVERY_NOT_IN_PENDING
		);
		this.status = DeliveryStatus.PREPARING;
	}

	public void readyForShipment() {
		Preconditions.validate(
			this.status == DeliveryStatus.PREPARING,
			ErrorCode.DELIVERY_NOT_IN_PREPARING
		);
		this.status = DeliveryStatus.READY;
	}

	public void ship() {
		Preconditions.validate(
			this.status == DeliveryStatus.READY,
			ErrorCode.DELIVERY_NOT_IN_READY
		);
		this.status = DeliveryStatus.SHIPPING;
		this.shippedAt = LocalDateTime.now();
	}

	public void complete() {
		Preconditions.validate(
			this.status == DeliveryStatus.SHIPPING,
			ErrorCode.DELIVERY_NOT_IN_SHIPPING
		);
		this.status = DeliveryStatus.DELIVERED;
		this.deliveredAt = LocalDateTime.now();
	}

	public void cancel() {
		boolean isCancellable =
			this.status != DeliveryStatus.SHIPPING &&
				this.status != DeliveryStatus.DELIVERED;

		Preconditions.validate(
			isCancellable,
			ErrorCode.DELIVERY_CANCEL_NOT_ALLOWED
		);
		this.status = DeliveryStatus.CANCELLED;
	}
}
