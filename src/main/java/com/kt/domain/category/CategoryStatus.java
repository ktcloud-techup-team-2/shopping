package com.kt.domain.category;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryStatus {
	ACTIVE("활성"),
	INACTIVE("비활성");
	private final String description;
}