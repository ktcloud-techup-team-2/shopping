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
	private Long deliverId;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private DeliveryStatus status;

	public static DeliveryStatusHistory create(Long deliverId, DeliveryStatus status) {
		var history = new DeliveryStatusHistory();
		history.deliverId = deliverId;
		history.status = status;
		return history;
	}
}
