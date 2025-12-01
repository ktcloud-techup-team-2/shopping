package com.kt.domain.delivery;

import com.kt.common.jpa.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Courier extends BaseTimeEntity {
	@Column(nullable = false, unique = true)
	private String code;
	@Column(nullable = false)
	private String name;
	@Column(nullable = false)
	private Boolean isActive;

	public static Courier create(String code, String name) {
		Courier courier = new Courier();
		courier.code = code;
		courier.name = name;
		courier.isActive = true;
		return courier;
	}

	public void update(String name, Boolean isActive) {
		this.name = name;
		this.isActive = isActive;
	}
}
