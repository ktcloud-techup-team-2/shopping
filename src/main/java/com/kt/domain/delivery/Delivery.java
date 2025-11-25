package com.kt.domain.delivery;

import java.time.LocalDateTime;

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
		if (this.status != DeliveryStatus.PENDING) {
			throw new IllegalStateException("주문접수 상태에서만 준비를 시작할 수 있습니다");
		}
		this.status = DeliveryStatus.PREPARING;
	}

	public void readyForShipment() {
		if (this.status != DeliveryStatus.PREPARING) {
			throw new IllegalStateException("상품준비중 상태에서만 출고준비완료로 변경할 수 있습니다");
		}
		this.status = DeliveryStatus.READY;
	}

	public void ship() {
		if (this.status != DeliveryStatus.READY) {
			throw new IllegalStateException("출고준비완료 상태에서만 배송을 시작할 수 있습니다");
		}
		this.status = DeliveryStatus.SHIPPING;
		this.shippedAt = LocalDateTime.now();
	}

	public void complete() {
		if (this.status != DeliveryStatus.SHIPPING) {
			throw new IllegalStateException("배송중 상태에서만 배송완료 처리할 수 있습니다");
		}
		this.status = DeliveryStatus.DELIVERED;
		this.deliveredAt = LocalDateTime.now();
	}

	public void cancel() {
		if (this.status == DeliveryStatus.SHIPPING ||
			this.status == DeliveryStatus.DELIVERED) {
			throw new IllegalStateException("배송중이거나 완료된 주문은 취소할 수 없습니다");
		}
		this.status = DeliveryStatus.CANCELLED;
	}
}
