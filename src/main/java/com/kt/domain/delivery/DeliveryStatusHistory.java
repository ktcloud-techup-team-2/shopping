package com.kt.domain.delivery;

import com.kt.common.jpa.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class DeliveryStatusHistory extends BaseTimeEntity {
	@Column(nullable = false)
	private Long deliveryId;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private DeliveryStatus status;

	public static DeliveryStatusHistory create(Long deliveryId, DeliveryStatus status) {
		var history = new DeliveryStatusHistory();
		history.deliveryId = deliveryId;
		history.status = status;
		return history;
	}
}
