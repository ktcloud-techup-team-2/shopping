package com.kt.domain.inventory;

import com.kt.common.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "processed_inbound_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedInboundEvent extends BaseTimeEntity {

	@Column(nullable = false, unique = true)
	private String eventId;

	private ProcessedInboundEvent(String eventId) {
		this.eventId = eventId;
	}

	public static ProcessedInboundEvent create(String eventId) {
		return new ProcessedInboundEvent(eventId);
	}
}
